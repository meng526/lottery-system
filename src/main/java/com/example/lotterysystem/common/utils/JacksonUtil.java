package com.example.lotterysystem.common.utils;

import com.fasterxml.jackson.core.JacksonException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.json.JsonParseException;

import java.util.List;
import java.util.concurrent.Callable;

public class JacksonUtil {
    public JacksonUtil(){

    }

    private static final ObjectMapper OBJECT_MAPPER;

    static{
        OBJECT_MAPPER=new ObjectMapper();
    }

    public static ObjectMapper getObjectMapper(){
        return OBJECT_MAPPER;
    }

    private static <T> T tryParse(Callable<T> parser){
        return tryParse(parser, JacksonException.class);
    }

    private static  <T> T tryParse(Callable<T> parser, Class<? extends Exception> check) {
        try {
            return parser.call();
        } catch (Exception var4) {
            if (check.isAssignableFrom(var4.getClass())) {
                throw new JsonParseException(var4);
            }

            throw new IllegalStateException(var4);
        }
    }

    public static String writeValueAsString(Object object){
        return tryParse(()->{
            return JacksonUtil.getObjectMapper().writeValueAsString(object);
        });
    }

    public static <T> T readValue(String content,Class<T> valueType){
        return tryParse(()->{
            return JacksonUtil.getObjectMapper().readValue(content,valueType);
        });
    }

    public static <T> T readListValue(String content,Class<?> valueType){
        JavaType javaType = JacksonUtil.getObjectMapper().getTypeFactory()
                .constructParametricType(List.class,valueType);
        return tryParse(()->{
            return JacksonUtil.getObjectMapper().readValue(content,javaType);
        });
    }
}
