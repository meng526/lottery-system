package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author: yibo
 */
@Data
public class WinningRecordDTO {
    /**
     * 中奖者id
     */
    private Long winnerId;

    /**
     * 中奖者姓名
     */
    private String winnerName;

    /**
     * 奖品名
     */
    private String prizeName;

    /**
     * 等级
     */
    private ActivityPrizeTiersEnum prizeTier;

    /**
     * 中奖时间
     */
    private Date winningTime;

}
