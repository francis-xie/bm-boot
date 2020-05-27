package com.emis.security.common.config;

import com.emis.security.common.evaluator.UserPermissionEvaluator;
import com.emis.security.common.filter.JWTAuthenticationTokenFilter;
import com.emis.security.common.handler.*;
import com.emis.security.common.provider.UserLoginProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

/**
 * @Description:
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //开启权限注解,默认是关闭的
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    static String[] EXCLUDEPATH = {"/index", "/login/**", "/v2/api-docs", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**"};
    /**
     * 自定义登录成功处理器
     */
    final UserLoginSuccessHandler userLoginSuccessHandler;
    /**
     * 自定义登录失败处理器
     */
    final UserLoginFailureHandler userLoginFailureHandler;
    /**
     * 自定义注销成功处理器
     */
    final UserLogoutSuccessHandler userLogoutSuccessHandler;
    /**
     * 自定义暂无权限处理器
     */
    final UserNotPermissionHandler userNotPermissionHandler;
    /**
     * 自定义未登录的处理器
     */
    final UserNotLoginHandler userNotLoginHandler;
    /**
     * 自定义登录逻辑验证器
     */
    final UserLoginProvider userLoginProvider;

    public SecurityConfig(UserLoginSuccessHandler userLoginSuccessHandler,
                          UserLoginFailureHandler userLoginFailureHandler,
                          UserLogoutSuccessHandler userLogoutSuccessHandler,
                          UserNotPermissionHandler userNotPermissionHandler,
                          UserNotLoginHandler userNotLoginHandler,
                          UserLoginProvider userLoginProvider) {
        this.userLoginSuccessHandler = userLoginSuccessHandler;
        this.userLoginFailureHandler = userLoginFailureHandler;
        this.userLogoutSuccessHandler = userLogoutSuccessHandler;
        this.userNotPermissionHandler = userNotPermissionHandler;
        this.userNotLoginHandler = userNotLoginHandler;
        this.userLoginProvider = userLoginProvider;
    }

    /**
     * 加密方式
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注入自定义PermissionEvaluator
     */
    @Bean
    public DefaultWebSecurityExpressionHandler userSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setPermissionEvaluator(new UserPermissionEvaluator());
        return handler;
    }

    /**
     * 配置登录验证逻辑
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        // 这里可启用我们自己的登陆验证逻辑
        auth.authenticationProvider(userLoginProvider);
    }

    /**
     * 配置security的控制逻辑
     *
     * @param http 请求
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 从配置文件获取不用进行权限验证的请求或资源
                .antMatchers(EXCLUDEPATH).permitAll()
                // 其他的需要登陆后才能访问
                .anyRequest().authenticated()
                .and()
                // 配置未登录自定义处理类
                .httpBasic().authenticationEntryPoint(userNotLoginHandler)
                .and()
                // 配置登录地址
                .formLogin()
                .loginProcessingUrl("/login/userLogin")
                // 配置登录成功自定义处理类
                .successHandler(userLoginSuccessHandler)
                // 配置登录失败自定义处理类
                .failureHandler(userLoginFailureHandler)
                .and()
                // 配置退出地址
                .logout()
                .logoutUrl("/login/userLogout")
                // 配置用户登出自定义处理类
                .logoutSuccessHandler(userLogoutSuccessHandler)
                .and()
                // 配置没有权限自定义处理类
                .exceptionHandling().accessDeniedHandler(userNotPermissionHandler)
                .and()
                // 开启跨域
                .cors()
                .and()
                // 取消跨站请求伪造防护
                .csrf().disable();
        // 基于Token不需要session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 禁用缓存
        http.headers().cacheControl();
        // 添加JWT过滤器
        http.addFilter(new JWTAuthenticationTokenFilter(authenticationManager()));
    }
}