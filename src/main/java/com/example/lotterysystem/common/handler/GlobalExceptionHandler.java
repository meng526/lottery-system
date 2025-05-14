package com.example.lotterysystem.common.handler;


import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.pojo.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> serviceException(ServiceException e){
        logger.error("serviceException:"+e.getMsg());
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMsg());
    }

    @ExceptionHandler(value = ControllerException.class)
    public CommonResult<?> controllerException(ControllerException e){
        logger.error("controllerException:"+e.getMsg());
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMsg());
    }

    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> exception(Exception e){
        logger.error("exception:"+e.getMessage());
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMessage());
    }

}
