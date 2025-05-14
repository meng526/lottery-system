package com.example.lotterysystem.common.interceptor;

import com.example.lotterysystem.common.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("user_token");
        log.info("preHandle:获取token = {}",token);
        log.info("preHandle:获取path = {}",request.getRequestURI());
        Claims claim = JWTUtil.parseJWT(token);
        if(null==claim){
            log.error("解析令牌失败！");
            response.setStatus(401);
            return false;
        }
        log.info("解析令牌成功，放行！");
        return true;
    }
}
