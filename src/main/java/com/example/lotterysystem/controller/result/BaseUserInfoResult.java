package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
@Data
public class BaseUserInfoResult implements Serializable {
    private Long userId;
    private String userName;
    private String identity;
}
