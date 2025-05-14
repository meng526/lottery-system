package com.example.lotterysystem.service.activityStatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.ActivityMapper;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrizeOperator extends AbstractActivityOperator {

    @Autowired
    ActivityMapper activityMapper;

    @Autowired
    ActivityPrizeMapper activityPrizeMapper;


    @Override
    public Integer sequence() {
        return 1;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        Long prizeId = convertActivityStatusDTO.getPrizeId();
        ActivityStatusEnum status = convertActivityStatusDTO.getTargetActivityStatus();
        if(null==activityId
                || null==status || null == prizeId){
            return false;
        }
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectPrizeById(activityId,prizeId);
        if(null==activityPrizeDO){
            return false;
        }
        if(activityPrizeDO.getStatus().equalsIgnoreCase(status.name())){
            return false;
        }
        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
       try {
           activityPrizeMapper.updatePrizeStatus(convertActivityStatusDTO.getActivityId(),
                   convertActivityStatusDTO.getPrizeId(),
                   convertActivityStatusDTO.getTargetPrizeStatus());
           return true;
       }catch (Exception e){
           return false;
       }
    }
}
