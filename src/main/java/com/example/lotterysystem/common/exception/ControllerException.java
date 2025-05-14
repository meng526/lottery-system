package com.example.lotterysystem.common.exception;

import com.example.lotterysystem.common.errorcode.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ControllerException extends RuntimeException {
    private Integer code;
    private String msg;

    public ControllerException(){

    }
    public ControllerException(Integer code,String msg){
        this.code=code;
        this.msg=msg;
    }

    public ControllerException(ErrorCode errorCode){
        this.code=errorCode.getCode();
        this.msg=errorCode.getMsg();
    }
}
