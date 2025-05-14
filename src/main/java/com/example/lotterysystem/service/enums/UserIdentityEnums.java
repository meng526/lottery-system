package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserIdentityEnums {

    ADMIN("管理员"),
    NORMAL("普通用户");

    private final String message;

    public static UserIdentityEnums forName(String name){
        for(UserIdentityEnums userIdentityEnum:UserIdentityEnums.values()){
            if(userIdentityEnum.name().equalsIgnoreCase(name)){
                return userIdentityEnum;
            }
        }
        return null;
    }

}
