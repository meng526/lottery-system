package com.example.lotterysystem.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JWTUtil;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RegexUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import com.example.lotterysystem.dao.mapper.UserMapper;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnums;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.ShortBufferException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserRegisterDTO register(UserRegisterParam param) {
        checkRegisterInfo(param);
        //数据落库
        UserDO userDO=new UserDO();
        userDO.setUserName(param.getName());
        userDO.setEmail(param.getMail());
        userDO.setPhoneNumber(new Encrypt(param.getPhoneNumber()));
        userDO.setIdentity(param.getIdentity());
        if(StringUtils.hasText(param.getPassword())){
            userDO.setPassword(DigestUtil.sha256Hex(param.getPassword()));
        }
        userMapper.insert(userDO);

        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUserId(userDO.getId());
        return userRegisterDTO;
    }

    @Override
    public UserLoginDTO login(UserLoginParam param) {

        if(null==param){
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_INFO_NOT_EXIST);
        }
        UserLoginDTO userLoginDTO;
        if(param instanceof UserPasswordLoginParam ){
            UserPasswordLoginParam userParam = (UserPasswordLoginParam) param;
            userLoginDTO =loginByUserPassword(userParam);
        }else if(param instanceof ShortMessageLoginParam ){
            ShortMessageLoginParam userParam = (ShortMessageLoginParam) param;
            userLoginDTO=loginByShortMessage(userParam);
        }else{
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_INFO_NOT_EXIST);
        }


        return userLoginDTO;
    }

    private UserLoginDTO loginByShortMessage(ShortMessageLoginParam userParam) {
        UserDO userDO;
        if(RegexUtil.checkMobile(userParam.getLoginMobile())){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        userDO=userMapper.queryByPhoneNumber(new Encrypt(DigestUtil.sha256Hex(userParam.getLoginMobile())));

        if(null==userDO){
            throw new ServiceException(ServiceErrorCodeConstants.USER_INFO_IS_EMPTY);
        }
        if(StringUtils.hasText(userParam.getMandatoryIdentity())
                &&!userParam.getMandatoryIdentity().equalsIgnoreCase(userDO.getIdentity())){
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }
        /*
        * 校验验证码
        * */

        UserLoginDTO userLoginDTO=new UserLoginDTO();
        Map<String,Object> claim = new HashMap<>();
        claim.put("id",userDO.getId());
        claim.put("identity",userDO.getIdentity());
        String token = JWTUtil.genJwt(claim);
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnums.forName(userDO.getIdentity()));
        return userLoginDTO;
    }

    private UserLoginDTO loginByUserPassword(UserPasswordLoginParam userParam) {
        UserDO userDO;
        if(RegexUtil.checkMobile(userParam.getLoginName())){
            userDO=userMapper.queryByPhoneNumber(new Encrypt(userParam.getLoginName()));
        }else if(RegexUtil.checkMail(userParam.getLoginName())){
            userDO=userMapper.queryByEmail(userParam.getLoginName());
        }else {
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_NOT_EXIST);
        }
        if(null==userDO){
            throw new ServiceException(ServiceErrorCodeConstants.USER_INFO_IS_EMPTY);
        }
        if(StringUtils.hasText(userParam.getMandatoryIdentity())
                &&!userParam.getMandatoryIdentity().equalsIgnoreCase(userDO.getIdentity())){
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }
        if(!DigestUtil.sha256Hex(userParam.getPassword()).equals(userDO.getPassword())){
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }

        UserLoginDTO userLoginDTO=new UserLoginDTO();
        Map<String,Object> claim = new HashMap<>();
        claim.put("id",userDO.getId());
        claim.put("identity",userDO.getIdentity());
        String token = JWTUtil.genJwt(claim);
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnums.forName(userDO.getIdentity()));
        return userLoginDTO;
    }

    private void checkRegisterInfo(UserRegisterParam param) {
        if(null==param){
            throw new ServiceException(ServiceErrorCodeConstants.REGISTER_PARAM_IS_EMPTY);
        }
        // 校验邮箱格式 xxx@xxx.xxx
        if (!RegexUtil.checkMail(param.getMail())) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_ERROR);
        }
        // 校验手机号格式
        if(!RegexUtil.checkMobile(param.getPhoneNumber())){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }

        // 校验身份信息
        if(null==UserIdentityEnums.forName(param.getIdentity())){
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }

        // 校验管理员密码必填
        if(param.getIdentity().equalsIgnoreCase(UserIdentityEnums.ADMIN.name())
            && !StringUtils.hasText(param.getPassword())){
            throw new ServiceException(ServiceErrorCodeConstants.ADMIN_PASSWORD_IS_EMPTY);
        }

        // 密码校验，至少6位
        if(StringUtils.hasText(param.getPassword()) &&
            !RegexUtil.checkPassword(param.getPhoneNumber())){
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }

        // 校验邮箱是否被使用
        if(checkMailUsed(param.getMail())){
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_USED);
        }

        // 校验手机号是否被使用
        if(checkPhoneNumberUsed(param.getPhoneNumber())){
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_USED);
        }

    }

    private boolean checkMailUsed(String mail) {
        int count = userMapper.countByMail(mail);
        return count>0;
    }
    private boolean checkPhoneNumberUsed(String phoneNumber){
        int count = userMapper.countByPhoneNumber(new Encrypt(phoneNumber));
        return count>0;
    }

    @Override
    public List<UserDTO> findUserInfo(UserIdentityEnums identity) {
        String identityString = null==identity?null:identity.name();
        List<UserDO> userDOList = userMapper.selectUserListByIdentity(identityString);
        List<UserDTO> userDTOList = userDOList.stream()
                .map(userDO -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserId(userDO.getId());
                    userDTO.setUserName(userDO.getUserName());
                    userDTO.setEmail(userDO.getEmail());
                    userDTO.setPhoneNumber(userDO.getPhoneNumber());
                    userDTO.setIdentity(UserIdentityEnums.forName(userDO.getIdentity()));
                    return userDTO;
                }).collect(Collectors.toList());
        return userDTOList;
    }
}
