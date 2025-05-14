package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnums;

import java.util.List;

public interface UserService {

    UserRegisterDTO register(UserRegisterParam userRegisterParam);

    UserLoginDTO login(UserLoginParam param);

    List<UserDTO> findUserInfo(UserIdentityEnums identity);
}