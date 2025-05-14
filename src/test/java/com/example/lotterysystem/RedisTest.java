package com.example.lotterysystem;

import com.example.lotterysystem.common.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisUtil redisUtil;
    @Test
    void redisTest(){
        stringRedisTemplate.opsForValue().set("key10","value10");
        System.out.println("读取redis存的值key10 = "+stringRedisTemplate.opsForValue().get("key10"));
    }
    @Test
    void delTest(){
        redisUtil.set("key2","value2");
        redisUtil.set("key3","value3");
        redisUtil.set("key4","value4");
        redisUtil.set("key5","value5");
        System.out.println("delTest:"+redisUtil.del("key2","key3","key4","key5"));
    }
}
