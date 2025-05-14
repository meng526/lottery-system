package com.example.lotterysystem.controller.result;

import com.example.lotterysystem.service.enums.UserIdentityEnums;
import lombok.Data;

@Data
public class UserLoginResult {
    private String token;
    private String identity;
}
