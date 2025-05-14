package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityPrizeDO extends BaseDO {

    private Long activityId;
    private Long prizeAmount;
    private Long prizeId;
    private String prizeTiers;
    private String status;

}
