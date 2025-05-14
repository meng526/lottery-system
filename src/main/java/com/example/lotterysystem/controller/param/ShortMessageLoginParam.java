package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShortMessageLoginParam extends UserLoginParam{
    @NotBlank(message = "手机号不能为空！！")
    private String loginMobile;
    @NotBlank(message = "验证码不能为空！！")
    private String verificationCode;
}
