package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPasswordLoginParam  extends UserLoginParam{
    @NotBlank(message = "用户名不能为空！！")
    private String loginName;
    @NotBlank(message = "密码不能为空！！")
    private String password;
}
