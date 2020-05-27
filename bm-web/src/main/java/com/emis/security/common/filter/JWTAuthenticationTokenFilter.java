package com.emis.security.common.filter;

import com.alibaba.fastjson.JSONObject;
import com.emis.security.model.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @Description: JWT接口请求校验拦截器请求接口时会进入这里验证Token是否合法和过期
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Slf4j
public class JWTAuthenticationTokenFilter extends BasicAuthenticationFilter {
    public JWTAuthenticationTokenFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {
        // 获取请求头中JWT的Token
        String tokenHeader = request.getHeader("Authorization");
        if (null != tokenHeader) {
            try {
                // 解析JWT
                Claims claims = Jwts.parser()
                        .setSigningKey("JWTSecret")
                        .parseClaimsJws(tokenHeader)
                        .getBody();
                // 获取用户名
                String username = claims.getSubject();
                String userId = claims.getId();
                if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(userId)) {
                    // 获取角色
                    Set<GrantedAuthority> authorities = new HashSet<>();
                    String authority = claims.get("authorities").toString();
                    if (!StringUtils.isEmpty(authority)) {
                        List<Map<String, String>> authorityMap = JSONObject.parseObject(authority, List.class);
                        authorityMap.forEach(
                                role -> authorities.add(new SimpleGrantedAuthority(role.get("authority")))
                        );
                    }
                    //组装参数
                    UserModel userModel = new UserModel();
                    userModel.setUserName(claims.getSubject()).setUserId(Integer.parseInt(claims.getId())).setAuthorities(authorities);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userModel, userId, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                log.info("Token过期");
            } catch (Exception e) {
                log.info("Token无效");
            }
        }
        filterChain.doFilter(request, response);
        return;
    }
}
