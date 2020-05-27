package com.emis.security.common.handler;

import com.emis.security.common.response.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description: 登录失败处理类
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Slf4j
@Component
public class UserLoginFailureHandler implements AuthenticationFailureHandler {
    /**
     * 登录失败返回结果
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        // 这些对于操作的处理类可以根据不同异常进行不同处理
        if (exception instanceof UsernameNotFoundException) {
            log.info("【登录失败】" + exception.getMessage());
            ServerResponse.createResponseEnumJson(response, ServerResponse.userNameOrPasswordError());
        }
        if (exception instanceof LockedException) {
            log.info("【登录失败】" + exception.getMessage());
            ServerResponse.createResponseEnumJson(response, ServerResponse.userLock());
        }
        if (exception instanceof BadCredentialsException) {
            log.info("【登录失败】" + exception.getMessage());
            ServerResponse.createResponseEnumJson(response, ServerResponse.userNameOrPasswordError());
        }
        ServerResponse.createResponseEnumJson(response, ServerResponse.createByError());
    }
}
