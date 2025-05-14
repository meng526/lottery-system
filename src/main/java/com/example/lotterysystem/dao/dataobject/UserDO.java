package com.example.lotterysystem.dao.dataobject;

import com.example.lotterysystem.service.enums.UserIdentityEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDO{

    private String userName;
    private String email;
    private Encrypt phoneNumber;
    private String password;
    private String identity;

}
