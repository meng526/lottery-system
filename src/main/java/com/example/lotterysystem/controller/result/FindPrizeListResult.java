package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FindPrizeListResult implements Serializable {
    private int total;

    private List<PrizeInfo> records;

    @Data
    public static class PrizeInfo implements Serializable{
        private Long prizeId;
        private String prizeName;
        private String description;
        private BigDecimal price;
        private String imageUrl;
    }
}
