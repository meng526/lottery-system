package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.config.DirectRabbitConfig;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.*;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.EXCHANGE_NAME;
import static com.example.lotterysystem.common.config.DirectRabbitConfig.ROUTING;
@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {
    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeServiceImpl.class);

    private static final Long WINNING_RECORDS_TIMEOUT = 60*60*24*2L;

    private final String WINNING_RECORDS_PREFIX = "WINNING_RECORDS_";
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ActivityMapper aMapper;

    @Autowired
    ActivityPrizeMapper apMapper;

    @Autowired
    ActivityUserMapper auMapper;

    @Autowired
    PrizeMapper pMapper;

    @Autowired
    UserMapper uMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    WinningRecordMapper winningRecordMapper;

    @Override
    public void drawPrize(DrawPrizeParam param) {
        Map<String,String> map = new HashMap<>();
        map.put("messageId",String.valueOf(UUID.randomUUID()));
        map.put("messageData", JacksonUtil.writeValueAsString(param));
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTING,map);
        logger.info("mq消息发送成功：map={}", JacksonUtil.writeValueAsString(map));
    }

    @Override
    public boolean checkDrawPrizeParam(DrawPrizeParam param) {
        ActivityDO aDO = aMapper.selectActivityById(param.getActivityId());
        ActivityPrizeDO apDO = apMapper.selectActivityPrizeByActivityIdAndPrizeId(param.getActivityId(),param.getPrizeId());

        if(null==aDO || null==apDO){
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY.getMsg());
            return false;
        }

        if(aDO.getStatus().equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())){
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_COMPLETED.getMsg());
            return false;
        }
        if(apDO.getStatus().equalsIgnoreCase(ActivityPrizeStatusEnum.COMPLETED.name())){
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_PRIZE_COMPLETED.getMsg());
            return false;
        }
        if (apDO.getPrizeAmount() != param.getWinnerList().size()) {
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_ERROR.getMsg());
            return false;
        }
        return true;
    }

    @Override
    public List<WinningRecordDO> saveWinnerRecords(DrawPrizeParam param) {
        // 查询相关信息：活动、人员、奖品、活动关联奖品
        ActivityDO aDO = aMapper.selectActivityById(param.getActivityId());
        PrizeDO pDO =  pMapper.selectPrizeById(param.getPrizeId());
        ActivityPrizeDO apDO = apMapper.selectActivityPrizeByActivityIdAndPrizeId(param.getActivityId(),param.getPrizeId());
        List<UserDO> auDO = uMapper.selectUserByIds(param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList()));



        // 构造中奖者记录，保存
        List<WinningRecordDO> winningRecordDOList = auDO.stream()
                .map(userDO -> {
                    WinningRecordDO winningDO = new WinningRecordDO();
                    winningDO.setActivityId(param.getActivityId());
                    winningDO.setActivityName(aDO.getActivityName());
                    winningDO.setPrizeId(param.getPrizeId());
                    winningDO.setPrizeName(pDO.getName());
                    winningDO.setPrizeTier(apDO.getPrizeTiers());
                    winningDO.setWinnerId(userDO.getId());
                    winningDO.setWinnerName(userDO.getUserName());
                    winningDO.setWinnerEmail(userDO.getEmail());
                    winningDO.setWinnerPhoneNumber(userDO.getPhoneNumber());
                    winningDO.setWinningTime(param.getWinningTime());
                    return winningDO;
                }).collect(Collectors.toList());
        winningRecordMapper.batchInsert(winningRecordDOList);

        // 缓存中奖者记录
        // 1、缓存奖品维度中奖记录(WinningRecord_activityId_prizeId, winningRecordDOList（奖品维度的中奖名单）)
        cacheWinningRecords(param.getActivityId()+"_"+param.getPrizeId(),
                winningRecordDOList,
                WINNING_RECORDS_TIMEOUT);

        // 2、缓存活动维度中奖记录(WinningRecord_activityId, winningRecordDOList(活动维度的中奖名单))
        // 当活动已完成再去存放活动维度中奖记录
        if (aDO.getStatus()
                .equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())) {
            // 查询活动维度的全量中奖记录
            List<WinningRecordDO> allList = winningRecordMapper.selectByActivityId(param.getActivityId());
            cacheWinningRecords(String.valueOf(param.getActivityId()),
                    allList,
                    WINNING_RECORDS_TIMEOUT);
        }
        return winningRecordDOList;

    }

    @Override
    public void deleteRecords(Long activityId, Long prizeId) {
        if (null == activityId) {
            logger.warn("要删除中奖记录相关的活动id为空！");
            return;
        }
        winningRecordMapper.deleteRecords(activityId,prizeId);

        if(null!=prizeId){
            deleteWinningRecords(activityId+"_"+prizeId);
        }
        deleteWinningRecords(String.valueOf(activityId));

    }

    @Override
    public List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param) {
        String key = null==param.getPrizeId()?
                    String.valueOf(param.getActivityId()):param.getActivityId()+"_"+param.getPrizeId();
        List<WinningRecordDO> winningRecordDOList =  getWinningRecords(key);
        if(!CollectionUtils.isEmpty(winningRecordDOList)){
            return convertToWinningRecordDTOList(winningRecordDOList);
        }
        winningRecordDOList = winningRecordMapper.selectByActivityIdOrPrizeId(param.getActivityId(),param.getPrizeId());
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            logger.info("查询的中奖记录为空！param:{}",
                    JacksonUtil.writeValueAsString(param));
            return Arrays.asList();
        }
        cacheWinningRecords(key,winningRecordDOList,WINNING_RECORDS_TIMEOUT);
        return convertToWinningRecordDTOList(winningRecordDOList);


    }

    private List<WinningRecordDTO> convertToWinningRecordDTOList(List<WinningRecordDO> winningRecordDOList) {
        return winningRecordDOList.stream()
                .map(winningRecordDO -> {
                    WinningRecordDTO winningRecordDTO = new WinningRecordDTO();
                    winningRecordDTO.setWinnerId(winningRecordDO.getWinnerId());
                    winningRecordDTO.setWinnerName(winningRecordDO.getWinnerName());
                    winningRecordDTO.setPrizeName(winningRecordDO.getPrizeName());
                    winningRecordDTO.setPrizeTier(ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()));
                    winningRecordDTO.setWinningTime(winningRecordDO.getWinningTime());
                    return winningRecordDTO;
                }).collect(Collectors.toList());
    }

    private void deleteWinningRecords(String key) {
        try {
            if(redisUtil.hasKey(WINNING_RECORDS_PREFIX+key)){
                redisUtil.del(WINNING_RECORDS_PREFIX+key);
            }

        }catch (Exception e){
            logger.error("删除中奖记录缓存异常，key:{}", key);
        }
    }

    private void cacheWinningRecords(String key,
                                     List<WinningRecordDO> winningRecordDOList,
                                     Long timeOut) {
        String str = "";
        try {
            if (!StringUtils.hasText(key)
                    || CollectionUtils.isEmpty(winningRecordDOList)) {
                logger.warn("要缓存的内容为空！key:{}, value:{}",
                        key, JacksonUtil.writeValueAsString(winningRecordDOList));
                return;
            }
            str = JacksonUtil.writeValueAsString(winningRecordDOList);
            redisUtil.set(WINNING_RECORDS_PREFIX+key,str,timeOut);
        }catch (Exception e){
            logger.error("缓存中奖记录异常！key:{}, value:{}", WINNING_RECORDS_PREFIX + key, str);
        }

    }

    private List<WinningRecordDO> getWinningRecords(String key){
        try {
            if (!StringUtils.hasText(key)) {
                logger.warn("要缓存的内容为空！key:{}, ",
                        key);
                return Arrays.asList();
            }

          String str =  redisUtil.get(WINNING_RECORDS_PREFIX+key);
            return JacksonUtil.readListValue(str,WinningRecordDO.class);
        }catch (Exception e){
            logger.error("缓存中奖记录异常！key:{}", WINNING_RECORDS_PREFIX + key);
            return Arrays.asList();
        }
    }
}
