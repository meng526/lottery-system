package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterParam {
    @NotBlank(message = "名字不能为空！")
    private String name;

    @NotBlank(message = "邮箱不能为空！")
    private String mail;

    @NotBlank(message = "电话号码不能为空！")
    private String phoneNumber;

    private String password;

    @NotBlank(message = "身份信息不能为空！")
    private String identity;
}
