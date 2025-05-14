package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.UserIdentityEnums;
import lombok.Data;

@Data
public class UserLoginDTO {
    private String token;
    private UserIdentityEnums identity;
}
