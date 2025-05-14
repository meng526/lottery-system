package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivityUserStatusEnum {
    INIT(1, "初始化"),

    COMPLETED(2, "已被抽取");



    private final Integer code;

    private final String message;

    public static ActivityUserStatusEnum forName(String name) {
        for (ActivityUserStatusEnum activityStatusEnum : ActivityUserStatusEnum.values()) {
            if (activityStatusEnum.name().equalsIgnoreCase(name)) {
                return activityStatusEnum;
            }
        }
        return null;
    }
}
