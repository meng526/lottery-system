package com.example.lotterysystem.service.activityStatus.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.activityStatus.ActivityStatusManager;
import com.example.lotterysystem.service.activityStatus.operater.AbstractActivityOperator;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class ActivityStatusManagerImpl implements ActivityStatusManager {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStatusManagerImpl.class);

    @Autowired
    ActivityService activityService;

    @Autowired
    private Map<String, AbstractActivityOperator> operatorMap = new HashMap<>();

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {
        if (CollectionUtils.isEmpty(operatorMap)) {
            logger.warn("operatorMap 为空！");
            return;
        }
        Map<String, AbstractActivityOperator> currMap = new HashMap<>(operatorMap);

        Boolean update = false;
        update = processConvertStatus(convertActivityStatusDTO, currMap, 1);

        update = processConvertStatus(convertActivityStatusDTO, currMap, 2)||update;
        if (update) {
            activityService.cacheActivity(convertActivityStatusDTO.getActivityId());
        }
    }

    @Override
    public void rollbackHandlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {
        for(AbstractActivityOperator operator : operatorMap.values()){
            operator.convert(convertActivityStatusDTO);
        }
    }

    private Boolean processConvertStatus(ConvertActivityStatusDTO convertStatusDTO,
                         Map<String, AbstractActivityOperator> currMap,
                         Integer sequence){
        Boolean update = false;
        Iterator<Map.Entry<String,AbstractActivityOperator>> iterator = currMap.entrySet().iterator();
        while(iterator.hasNext()){
            AbstractActivityOperator operator = iterator.next().getValue();
            if(operator.sequence()!=sequence
                    || !operator.needConvert(convertStatusDTO)){
                continue;
            }

            if(!operator.convert(convertStatusDTO)){
                logger.error("{}状态转换失败！", operator.getClass().getName());
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_STATUS_CONVERT_ERROR);
            }
            update=true;
            iterator.remove();
        }

        return update;
    }
}
