package com.emis.security.common.handler;

import com.emis.security.common.jwt.JWTToken;
import com.emis.security.common.response.ServerResponse;
import com.emis.security.model.UserModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 登录成功处理类
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Slf4j
@Component
public class UserLoginSuccessHandler implements AuthenticationSuccessHandler {
    /**
     * 登录成功返回结果
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 组装JWT
        UserModel userModel = (UserModel) authentication.getPrincipal();
        ServerResponse.createResponseEnumJson(response, ServerResponse.createBySuccess(JWTToken.createAccessToken(userModel)));
    }
}
