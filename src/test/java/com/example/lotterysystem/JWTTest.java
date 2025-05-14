package com.example.lotterysystem;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;

@SpringBootTest
public class JWTTest {
    @Test
    public void genKey(){
        // 创建了一个密钥对象，使用HS256签名算法。
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // 将密钥编码为Base64字符串。
        String secretString = Encoders.BASE64.encode(key.getEncoded());

        // 结果：x2pQMNrJd7G/+xFdHb5pl3iGOLGT7jiL+f+ZEXa4LdA=
    }
}
