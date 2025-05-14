package com.example.lotterysystem.service.mq;

import cn.hutool.core.date.DateUtil;
import com.example.lotterysystem.common.config.ExecutorConfig;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.MailUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.dao.mapper.WinningRecordMapper;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.activityStatus.ActivityStatusManager;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.QUEUE_NAME;
@Component
@RabbitListener(queues = QUEUE_NAME)
public class MqReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MqReceiver.class);

    @Autowired
    DrawPrizeService drawPrizeService;

    @Autowired
    ActivityStatusManager activityStatusManager;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    ActivityPrizeMapper apMapper;

    @Autowired
    WinningRecordMapper winningRecordMapper;

    @Autowired
    MailUtil mailUtil;

    @RabbitHandler
    public void process(Map<String,String> message){
        logger.info("MQ成功接收到消息，message:{}",
                JacksonUtil.writeValueAsString(message));
        String paramString = message.get("messageData");
        DrawPrizeParam param = JacksonUtil.readValue(paramString,DrawPrizeParam.class);

        try {

            // 校验抽奖请求是否有效
            // 1、有可能前端发起两个一样的抽奖请求，对于param来说也是一样的两个请求
            // 2、param：最后一个奖项-》
            //      处理param1：活动完成、奖品完成
            //      处理param2: 回滚活动、奖品状态
            if (!drawPrizeService.checkDrawPrizeParam(param)) {
                return;
            }

            // 状态扭转处理（重要！！ 设计模式）
            statusConvert(param);

            // 保存中奖者名单
            List<WinningRecordDO> winningRecordDOList = drawPrizeService.saveWinnerRecords(param);

            // 通知中奖者（邮箱、短信）
            // 抽奖之后的后续流程，异步（并发）处理
            syncExecute(winningRecordDOList);

        } catch (ServiceException e) {
            logger.error("处理 MQ 消息异常！{}:{}", e.getCode(), e.getMessage(), e);
            // 需要保证事务一致性（回滚）
            rollback(param);
            // 抛出异常: 消息重试（解决异常：代码bug、网络问题、服务问题）
            throw e;

        } catch (Exception e) {
            logger.error("处理 MQ 消息异常！", e);
            // 需要保证事务一致性（回滚）
            rollback(param);
            // 抛出异常
            throw e;
        }

    }

    private void rollback(DrawPrizeParam param) {
        if(!statusNeedRollback(param)){
            return;
        }
        rollbackStatus(param);

        if(!winnerNeedRollback(param)){
            return;
        }
        rollbackWinner(param);
    }

    private void rollbackStatus(DrawPrizeParam param) {
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.RUNNING);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.INIT);
        convertActivityStatusDTO.setUserIds(param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList()));
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.INIT);
        activityStatusManager.rollbackHandlerEvent(convertActivityStatusDTO);
    }

    private boolean statusNeedRollback(DrawPrizeParam param) {
        ActivityPrizeDO apDO = apMapper.selectActivityPrizeByActivityIdAndPrizeId(param.getActivityId(),param.getPrizeId());
        return ActivityStatusEnum.COMPLETED.name()
                .equalsIgnoreCase(apDO.getStatus());
    }
    private void rollbackWinner(DrawPrizeParam param) {
        drawPrizeService.deleteRecords(param.getActivityId(), param.getPrizeId());
    }

    private boolean winnerNeedRollback(DrawPrizeParam param) {
        int count = winningRecordMapper.countById(param.getActivityId(),param.getPrizeId());
        return count>0;
    }

    private void syncExecute(List<WinningRecordDO> winningRecordDOList) {
        threadPoolTaskExecutor.execute(()->sendMail(winningRecordDOList));

    }

    private void sendMail(List<WinningRecordDO> winningRecordDOList) {
        String context ="";
        for(WinningRecordDO winningRecordDO : winningRecordDOList){
            context = "Hi，" + winningRecordDO.getWinnerName() + "。恭喜你在"
                    + winningRecordDO.getActivityName() + "活动中获得"
                    + ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()).getMessage()
                    + "：" + winningRecordDO.getPrizeName() + "。获奖时间为"
                    + DateUtil.formatTime(winningRecordDO.getWinningTime()) + "，请尽快领 取您的奖励！";
            mailUtil.sendSampleMail(winningRecordDO.getWinnerEmail(),
                    "中奖通知", context);
        }

    }

    private void statusConvert(DrawPrizeParam param) {
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);
        convertActivityStatusDTO.setUserIds(
                param.getWinnerList().stream()
                    .map(DrawPrizeParam.Winner::getUserId)
                    .collect(Collectors.toList()));
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);
        activityStatusManager.handlerEvent(convertActivityStatusDTO);
    }

}
