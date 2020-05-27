package com.emis.security.common.enmun;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @Description: Response枚举
 * @Author: LiuRunYong
 * @Date: 2020/4/1
 **/

@Getter
@AllArgsConstructor
public enum ResponseEnum {

    /**
     * 响应成功
     */
    SUCCESS(200d, "成功"),
    /**
     * 响应失败
     */
    ERROR(400d, "失败"),
    /**
     * 用户名密码错误
     */
    USERNAME_PASSWORD_ERROR(401.1d, "用户名或密码错误"),
    /**
     * 用户冻结
     */
    USER_LOCK(401.2d, "用户冻结"),
    /**
     * 无对应资源权限
     */
    NO_ACCESS_PERMISSIONS(403d, "无对应资源权限"),
    /**
     * 系统异常
     */
    EXCEPTION(500d, "系统异常"),
    /**
     * 未登录
     */
    NOT_LOGIN(530d, "未登录");

    private final Double code;
    private final String desc;
}
