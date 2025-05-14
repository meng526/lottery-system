package com.example.lotterysystem.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PrizeDTO implements Serializable {

    /**
     * 奖品Id
     */
    private Long prizeId;
    /**
     * 奖品名
     */
    private String name;

    /**
     * 图片索引
     */
    private String imageUrl;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 描述
     */
    private String description;

}
