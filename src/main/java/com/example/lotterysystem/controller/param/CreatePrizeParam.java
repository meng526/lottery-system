package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class CreatePrizeParam{
    @NotBlank(message = "奖品名称不能为空")
    private String prizeName;

    private String description;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

}
