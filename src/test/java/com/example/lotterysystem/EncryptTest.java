package com.example.lotterysystem;


import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.mapper.UserMapper;
import com.example.lotterysystem.service.UserService;
import org.junit.jupiter.api.Test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;


@SpringBootTest
public class EncryptTest {
    /*@Test
    void classpathIsExisted() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = loader.getResources("");
        while (resources.hasMoreElements()) {
            System.out.println(resources.nextElement());
        }
    }*/
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    private static final Logger logger= LoggerFactory.getLogger(EncryptTest.class);
    @Test
    void sha256(){
        //加密
        String password = "123456789abc";
        String pass = DigestUtil.sha256Hex(password);
        logger.info("加密后的字符："+pass);
    }

    @Test
    void secureutil(){
        //生成秘钥
       AES aes =  SecureUtil.aes("123456789abcdefg".getBytes());

        //加密
        String jiami =  aes.encryptHex("123456789");
        logger.info("加密后的"+jiami);
        //解密
        String jiemi = aes.decryptStr(jiami);
        logger.info("解密后的"+jiemi);
    }

    @Test
    void testEncryptPhoneNumber(){
        userMapper.countByPhoneNumber(new Encrypt("13154678941"));
    }

}
