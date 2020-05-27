package com.emis.security.common.handler;

import com.emis.security.common.response.ServerResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description: 用户登出类
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Component
public class UserLogoutSuccessHandler implements LogoutSuccessHandler {
    /**
     * 用户登出返回结果
     * 这里应该让前端清除掉Token
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SecurityContextHolder.clearContext();
        ServerResponse.createResponseEnumJson(response, ServerResponse.createBySuccessMessage("登出成功"));

    }
}