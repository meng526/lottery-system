package com.example.lotterysystem.service.activityStatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.mapper.ActivityMapper;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivityOperator extends AbstractActivityOperator {

    @Autowired
    ActivityMapper activityMapper;

    @Autowired
    ActivityPrizeMapper activityPrizeMapper;

    @Override
    public Integer sequence() {
        return 2;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        ActivityStatusEnum status = convertActivityStatusDTO.getTargetActivityStatus();
        if(null==activityId || null==status){
            return false;
        }
        ActivityDO activityDO = activityMapper.selectActivityById(activityId);
        if(null==activityDO){
            return false;
        }
        if(activityDO.getStatus().equalsIgnoreCase(status.name())){
            return false;
        }
        int count = activityPrizeMapper.countPrize(activityId, ActivityPrizeStatusEnum.INIT);
        if(count>0){
            return false;
        }

        return true;

    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        try {
            activityMapper.updateActivityStatus(convertActivityStatusDTO.getActivityId(),convertActivityStatusDTO.getTargetActivityStatus());
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
