package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePrizeByActivityParam {
    @NotNull(message = "活动关联的奖品id不能为空！")
    private Long prizeId;
    @NotNull(message = "奖品数量不能为空！")
    private Long prizeAmount;
    @NotBlank(message = "奖品等奖不能为空！")
    private String prizeTiers;

}
