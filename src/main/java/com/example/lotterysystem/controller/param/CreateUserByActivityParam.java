package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserByActivityParam {
    @NotNull(message = "活动关联的人员id不能为空！")
    private Long userId;
    @NotBlank(message = "姓名不能为空！")
    private String userName;
}
