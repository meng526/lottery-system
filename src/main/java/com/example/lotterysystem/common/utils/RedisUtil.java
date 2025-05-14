package com.example.lotterysystem.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger logger= LoggerFactory.getLogger(RedisUtil.class);

    public boolean set(String key,String value){
        try{
            stringRedisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            logger.error("RedisUtil:set({},{})",key,value,e);
            return false;
        }
    }

    public boolean set(String key,String value,Long time){
        try{
            stringRedisTemplate.opsForValue().set(key,value,time, TimeUnit.SECONDS);
            return true;
        }catch (Exception e){
            logger.error("RedisUtil:set({},{},{})",key,value,time,e);
            return false;
        }
    }

    public String get(String key){
        try{
            return StringUtils.hasText(key)
                    ?stringRedisTemplate.opsForValue().get(key)
                    :null;
        }catch (Exception e){
            logger.error("RedisUtil:get({})",key,e);
            return null;
        }
    }

    public boolean del(String... keys){
        try{
            if(null==keys || keys.length==0){
                return false;
            }
            logger.info("RedisUtil:del({})",Arrays.asList(keys));
            stringRedisTemplate.delete(Arrays.asList(keys));
            return true;
        }catch (Exception e){
            logger.error("RedisUtil:del({})",keys,e);
            return false;
        }
    }
    public boolean hasKey(String key) {
        try {
            return StringUtils.hasText(key)
                    ? stringRedisTemplate.hasKey(key)
                    : false;
        } catch (Exception e) {
            logger.error("RedisUtil error, hasKey({})", key, e);
            return false;
        }

    }


}
