package com.example.lotterysystem.service.activityStatus.operater;

import cn.hutool.core.collection.CollectionUtil;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.mapper.ActivityUserMapper;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class UserOperator extends AbstractActivityOperator {
    @Autowired
    ActivityUserMapper activityUserMapper;

    @Override
    public Integer sequence() {
        return 1;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        List<Long> ids = convertActivityStatusDTO.getUserIds();
        ActivityStatusEnum status = convertActivityStatusDTO.getTargetActivityStatus();
        if(null==activityId
                || null==status || CollectionUtil.isEmpty(ids)){
            return false;
        }
        List<ActivityUserDO> activityUserDOList = activityUserMapper.selectUserByIds(activityId,ids);
        if(CollectionUtil.isEmpty(activityUserDOList)){
            return false;
        }
        for(ActivityUserDO userDO:activityUserDOList){
            if(userDO.getStatus().equalsIgnoreCase(status.name())){
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        try {
            activityUserMapper.updateActivityUserStatus(convertActivityStatusDTO.getActivityId(),
                    convertActivityStatusDTO.getUserIds(),
                    convertActivityStatusDTO.getTargetUserStatus());
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
