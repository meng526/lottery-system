package com.example.lotterysystem.common.pojo;

import com.example.lotterysystem.common.errorcode.ErrorCode;
import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;
@Data
public class CommonResult<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;

    public static <T> CommonResult<T> success(T data){
        CommonResult<T> result = new CommonResult();
        result.code= GlobalErrorCodeConstants.SUCCESS.getCode();
        result.msg = GlobalErrorCodeConstants.SUCCESS.getMsg();
        result.data=data;
        return result;
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode){
        return error(errorCode.getCode(),errorCode.getMsg());
    }

    public static <T> CommonResult<T> error(Integer code,String msg){
        Assert.isTrue(!GlobalErrorCodeConstants.SUCCESS.getCode().equals(code),"code 不是失败错误码");
        CommonResult<T> result = new CommonResult<>();
        result.code=code;
        result.msg=msg;
        result.data=null;
        return result;
    }

}
