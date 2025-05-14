package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityDO extends BaseDO{
    private String activityName;
    private String description;
    private String activityStatusEnum;
    private String status;
}
