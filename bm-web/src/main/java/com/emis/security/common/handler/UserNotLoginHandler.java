package com.emis.security.common.handler;

import com.emis.security.common.response.ServerResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description: 用户未登录处理类
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Component
public class UserNotLoginHandler implements AuthenticationEntryPoint {
    /**
     * 用户未登录返回结果
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        ServerResponse.createResponseEnumJson(response, ServerResponse.notLogin());
    }
}