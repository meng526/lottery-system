package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.controller.result.BaseUserInfoResult;
import com.example.lotterysystem.controller.result.UserLoginResult;
import com.example.lotterysystem.controller.result.UserRegisterResult;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @RequestMapping("/register")
    public CommonResult<UserRegisterResult> userRegister(@RequestBody @Validated UserRegisterParam userRegisterParam){
        logger.info("userRegister-->userRegisterParam:{}", JacksonUtil.writeValueAsString(userRegisterParam));
        UserRegisterDTO userRegisterDTO=userService.register(userRegisterParam);
        return CommonResult.success(convertToUserRegisterResult(userRegisterDTO));
    }


    private UserRegisterResult convertToUserRegisterResult(UserRegisterDTO userRegisterDTO){
        if(null==userRegisterDTO){
            throw new ControllerException(ControllerErrorCodeConstants.REGISTER_ERROR);
        }
        UserRegisterResult userRegisterResult=new UserRegisterResult();
        userRegisterResult.setUserId(userRegisterDTO.getUserId());
        return  userRegisterResult;
    }
    @RequestMapping("/password/login")
    public CommonResult<UserLoginResult> userPasswordLogin(
            @Validated @RequestBody UserPasswordLoginParam param){
        logger.info("userPasswordLogin:userPasswordParam = {}",JacksonUtil.writeValueAsString(param ));
        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(convertToUserLoginResult(userLoginDTO));
    }
    @RequestMapping("/message/login")
    public CommonResult<UserLoginResult> shortMessageLogin(
            @Validated @RequestBody ShortMessageLoginParam param){
        logger.info("shortMessageLogin:ShortMessageLoginParam = {}",JacksonUtil.writeValueAsString(param ));
        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(convertToUserLoginResult(userLoginDTO));
    }

    private UserLoginResult convertToUserLoginResult(UserLoginDTO userLoginDTO) {
        if(null==userLoginDTO){
            throw new ControllerException(ControllerErrorCodeConstants.LOGIN_ERROR);
        }
        UserLoginResult userLoginResult=new UserLoginResult();
        userLoginResult.setToken(userLoginDTO.getToken());
        userLoginResult.setIdentity(userLoginDTO.getIdentity().name());
        return userLoginResult;
    }
    @RequestMapping("/base-user/find-list")
    private CommonResult<List<BaseUserInfoResult>> findBaseUserInfo(String identity) {
        logger.info("findBaseUserInfo:identity = {}",identity);
        List<UserDTO> userDTOList = userService.findUserInfo(
                UserIdentityEnums.forName(identity)
        );
        return CommonResult.success(convertToList(userDTOList));
    }
    private List<BaseUserInfoResult> convertToList(List<UserDTO> userDTOList) {
        if(CollectionUtils.isEmpty(userDTOList)){
           return Arrays.asList();
        }
        return userDTOList.stream()
                .map(userDTO -> {
                    BaseUserInfoResult result = new BaseUserInfoResult();
                    result.setUserId(userDTO.getUserId());
                    result.setUserName(userDTO.getUserName());
                    result.setIdentity(userDTO.getIdentity().name());
                    return result;
                }).collect(Collectors.toList());
    }


}
