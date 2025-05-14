package com.example.lotterysystem.controller.param;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserLoginParam implements Serializable {
    private String mandatoryIdentity;
}
