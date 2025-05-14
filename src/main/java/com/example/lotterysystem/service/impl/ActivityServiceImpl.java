package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.CreatePrizeByActivityParam;
import com.example.lotterysystem.controller.param.CreateUserByActivityParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.ActivityDetailDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.views.AbstractView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
    private final String ACTIVITY_PREFIX = "ACTIVITY_";

    private final Long ACTIVITY_TIMEOUT = 60 * 60 * 24 * 3L;
    @Autowired
    UserMapper userMapper;

    @Autowired
    PrizeMapper prizeMapper;

    @Autowired
    ActivityMapper activityMapper;

    @Autowired
    ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    ActivityUserMapper activityUserMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及多表
    public CreateActivityDTO createActivity(CreateActivityParam param) {
        // 校验活动信息是否正确
        checkActivityInfo(param);
        logger.info("checkActivityInfo(param)");
        // 保存活动信息
        ActivityDO activityDO = new ActivityDO();
            activityDO.setActivityName(param.getActivityName());
            activityDO.setDescription(param.getDescription());
            activityDO.setStatus(ActivityStatusEnum.RUNNING.name());
            activityMapper.insert(activityDO);

        // 保存活动关联的奖品信息
        List<CreatePrizeByActivityParam> prizeParams  = param.getActivityPrizeList();
        List<ActivityPrizeDO> activityPrizeDOList = prizeParams.stream()
                .map(prizeParam->{
                    ActivityPrizeDO activityPrizeDO = new ActivityPrizeDO();
                    activityPrizeDO.setActivityId(activityDO.getId());
                    activityPrizeDO.setPrizeAmount(prizeParam.getPrizeAmount());
                    activityPrizeDO.setPrizeId(prizeParam.getPrizeId());
                    activityPrizeDO.setPrizeTiers(prizeParam.getPrizeTiers());
                    activityPrizeDO.setStatus(ActivityPrizeStatusEnum.INIT.name());
                    return activityPrizeDO;
                }).collect(Collectors.toList());
        try {
            activityPrizeMapper.batchInsert(activityPrizeDOList);
        }catch (Exception e){
            logger.info("e = {}",e);
        }

        logger.info("activityPrizeMapper.batchInsert(activityPrizeDOList)");
        // 保存活动关联的人员信息
        List<CreateUserByActivityParam> userParams = param.getActivityUserList();
        List<ActivityUserDO> activityUserDOList = userParams.stream()
                .map(userparam->{
                    ActivityUserDO activityUserDO = new ActivityUserDO();
                    activityUserDO.setActivityId(activityDO.getId());
                    activityUserDO.setUserId(userparam.getUserId());
                    activityUserDO.setUserName(userparam.getUserName());
                    activityUserDO.setStatus(ActivityUserStatusEnum.INIT.name());
                    return activityUserDO;
                }).collect(Collectors.toList());
        activityUserMapper.batchInsert(activityUserDOList);
        logger.info("activityUserMapper.batchInsert(activityUserDOList)");
        // 整合完整的活动信息，存放 redis
        // activityId: ActivityDetailDTO:活动+奖品+人员
        List<Long> prizeIds = param.getActivityPrizeList().stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<PrizeDO> prizeDOS = prizeMapper.batchSelectByIds(prizeIds);
        ActivityDetailDTO detailDTO = convertToActivityDetailDTO(activityDO,activityUserDOList,prizeDOS,activityPrizeDOList);
        // 先获取奖品基本属性列表
        // 获取需要查询的奖品id
        cacheActivity(detailDTO);
        logger.info("cacheActivity(detailDTO);");
        // 构造返回
        CreateActivityDTO createActivityDTO = new CreateActivityDTO();
        createActivityDTO.setActivityId(activityDO.getId());

        return createActivityDTO;
    }

    @Override
    public PageListDTO<ActivityDTO> findActivityList(PageParam param) {
        if(null == param){
            throw new ServiceException(ServiceErrorCodeConstants.FIND_ACTIVITY_LIST_PARAM_ERROR);
        }
        int total = activityMapper.count();
        List<ActivityDO> activityDOList = activityMapper.selectActivityList(param.offset(),param.getPageSize());
        List<ActivityDTO> activityDTOList = activityDOList.stream()
                .map(activityDO -> {
                    ActivityDTO activityDTO = new ActivityDTO();
                    activityDTO.setActivityId(activityDO.getId());
                    activityDTO.setActivityName(activityDO.getActivityName());
                    activityDTO.setDescription(activityDO.getDescription());
                    activityDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));
                    return activityDTO;
                }).collect(Collectors.toList());
        return new PageListDTO<>(total,activityDTOList);
    }

    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        ActivityDetailDTO detailDTO = getActivityFromCache(activityId);
        if(null!=detailDTO){
            logger.info("查询活动详细信息成功！detailDTO={}",
                    JacksonUtil.writeValueAsString(detailDTO));
            return detailDTO;
        }
        ActivityDO aDO = activityMapper.selectActivityById(activityId);
        List<ActivityUserDO> auDO = activityUserMapper.selectActivityUserByActivityId(activityId);
        List<ActivityPrizeDO> apDO = activityPrizeMapper.selectActivityPrizeByActivityId(activityId);
        List<Long> Ids = apDO.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<PrizeDO> pDO = prizeMapper.batchSelectByIds(Ids);
        detailDTO = convertToActivityDetailDTO(aDO,auDO,pDO,apDO);
        cacheActivity(detailDTO);
        return detailDTO;
    }

    @Override
    public void cacheActivity(Long activityId) {
        if(null==activityId){
            logger.warn("要缓存的活动信息不存在!");
        }

        ActivityDO aDO = activityMapper.selectActivityById(activityId);
        if (null == aDO) {
            logger.error("要缓存的活动id有误！");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_ERROR);
        }
        // 活动奖品表
        List<ActivityPrizeDO> apDOList =  activityPrizeMapper.selectActivityPrizeByActivityId(activityId);
        // 活动人员表
        List<ActivityUserDO> auDOList = activityUserMapper.selectActivityUserByActivityId(activityId);
        // 奖品表: 先获取要查询的奖品id
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);
        // 整合活动详细信息，存放redis
        cacheActivity(
                convertToActivityDetailDTO(aDO, auDOList,
                        pDOList, apDOList));
    }

    private void cacheActivity(ActivityDetailDTO detailDTO) {
        if(null==detailDTO || null==detailDTO.getActivityId()){
            logger.warn("要缓存的活动信息不存在!");
        }
        try{
            redisUtil.set(ACTIVITY_PREFIX+detailDTO.getActivityId(),
                    JacksonUtil.writeValueAsString(detailDTO),ACTIVITY_TIMEOUT);
        }catch (Exception e){
            logger.error("缓存活动异常，ActivityDetailDTO={}",
                    JacksonUtil.writeValueAsString(detailDTO),
                    e);
        }
    }

    private ActivityDetailDTO getActivityFromCache(Long activityId){
        if(null == activityId){
            throw new ServiceException(ServiceErrorCodeConstants.FIND_ACTIVITY_LIST_ID_ERROR);
        }

        try{
            String str = redisUtil.get(ACTIVITY_PREFIX+activityId);
            if(!StringUtils.hasText(str)){
                logger.info("获取缓存活动数据为空! key = {}",ACTIVITY_PREFIX+activityId);
                return null;
            }
            return JacksonUtil.readValue(str,ActivityDetailDTO.class);
        }catch (Exception e){
            logger.error("从缓存中获取活动信息异常，key={}", ACTIVITY_PREFIX + activityId, e);
            return null;
        }

    }

    private ActivityDetailDTO convertToActivityDetailDTO(ActivityDO activityDO, List<ActivityUserDO> activityUserDOList, List<PrizeDO> prizeDOS, List<ActivityPrizeDO> activityPrizeDOList) {
        ActivityDetailDTO activityDetailDTO = new ActivityDetailDTO();
        activityDetailDTO.setActivityId(activityDO.getId());
        activityDetailDTO.setActivityName(activityDO.getActivityName());
        activityDetailDTO.setDesc(activityDO.getDescription());
        activityDetailDTO.setStatus(ActivityStatusEnum.RUNNING);
        List<ActivityDetailDTO.PrizeDTO> prizeDTOList = activityPrizeDOList.stream()
                .map(aprizeDO->{
                    ActivityDetailDTO.PrizeDTO prizeDTO = new ActivityDetailDTO.PrizeDTO();
                    Optional<PrizeDO> OptionalPrizeDO = prizeDOS.stream()
                            .filter(prizeDO->prizeDO.getId().equals(aprizeDO.getPrizeId()))
                            .findFirst();
                    OptionalPrizeDO.ifPresent(prizeDO->{
                        prizeDTO.setPrizeId(prizeDO.getId());
                        prizeDTO.setName(prizeDO.getName());
                        prizeDTO.setImageUrl(prizeDO.getImageUrl());
                        prizeDTO.setPrice(prizeDO.getPrice());
                        prizeDTO.setDescription(prizeDO.getDescription());
                    });
                    prizeDTO.setTiers(ActivityPrizeTiersEnum.forName(aprizeDO.getPrizeTiers()));
                    prizeDTO.setPrizeAmount(aprizeDO.getPrizeAmount());
                    prizeDTO.setStatus(ActivityPrizeStatusEnum.forName(aprizeDO.getStatus()));
                    return prizeDTO;
                }).collect(Collectors.toList());
        activityDetailDTO.setPrizeDTOList(prizeDTOList);
        List<ActivityDetailDTO.UserDTO> userDTOList = activityUserDOList.stream()
                .map(userDO->{
                    ActivityDetailDTO.UserDTO userDTO = new ActivityDetailDTO.UserDTO();
                    userDTO.setUserId(userDO.getUserId());
                    userDTO.setUserName(userDO.getUserName());
                    userDTO.setStatus(ActivityUserStatusEnum.forName(userDO.getStatus()));
                    return userDTO;
                }).collect(Collectors.toList());
        activityDetailDTO.setUserDTOList(userDTOList);
        return activityDetailDTO;
    }

    private void checkActivityInfo(CreateActivityParam param) {

        if(null==param){
            throw new ServiceException(ServiceErrorCodeConstants.CREATE_ACTIVITY_INFO_IS_EMPTY);
        }

        List<Long> userIds = param.getActivityUserList()
                .stream()
                .map(CreateUserByActivityParam::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existUserIds = userMapper.selectExistByIds(userIds);
        if(CollectionUtils.isEmpty(existUserIds)){
            throw  new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
        }
        userIds.forEach(id->{
            if(!existUserIds.contains(id)){
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
            }
        });

        List<Long> prizeIds = param.getActivityPrizeList()
                .stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existPrizeIds = prizeMapper.selectExistByIds(prizeIds);
        if(CollectionUtils.isEmpty(existPrizeIds)){
            logger.info("existPrizeIds isEmpty");
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
        }
        prizeIds.forEach(id->{
            if(!existPrizeIds.contains(id)){
                logger.info("id不存在 id = {} existPrizeIds = {}",id,existPrizeIds);
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
            }
        });
        int userAmount = param.getActivityUserList().size();
        long prizeAmount = param.getActivityPrizeList()
                .stream()
                .mapToLong(CreatePrizeByActivityParam::getPrizeAmount)
                .sum();
        if(userAmount<prizeAmount){
            throw new ServiceException(ServiceErrorCodeConstants.USER_PRIZE_AMOUNT_ERROR);
        }
        // 校验活动奖品等奖有效性
        param.getActivityPrizeList().forEach(prize->{
            if(null== ActivityPrizeTiersEnum.forName(prize.getPrizeTiers())){

                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_TIERS_ERROR);
            }
        });


    }
}
