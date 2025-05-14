package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrizeDO extends BaseDO {

    private String name;

    private String description;

    private String imageUrl;

    private BigDecimal price;

}
