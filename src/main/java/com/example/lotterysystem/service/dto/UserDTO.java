package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.service.enums.UserIdentityEnums;
import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String userName;
    private String email;
    private Encrypt phoneNumber;
    private UserIdentityEnums identity;
}
