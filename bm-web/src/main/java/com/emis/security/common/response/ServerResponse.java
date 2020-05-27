package com.emis.security.common.response;


import com.alibaba.fastjson.JSON;
import com.emis.security.common.enmun.ResponseEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletResponse;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * @Description: 服务返回封装
 * @Author: LiuRunYong
 * * @Date: 2020/4/28
 **/

// 保证序列化json的时候,如果对象为null,则不会转化为json
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Slf4j
public class ServerResponse<T> implements Serializable {

    private Double status;
    private String msg;
    private String url;
    private T data;

    private ServerResponse(Double status) {
        this.status = status;
    }

    private ServerResponse(Double status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(Double status, String msg, T data) {
        this.data = data;
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(Double status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(Double status, String msg, String url) {
        this.status = status;
        this.msg = msg;
        this.url = url;
    }

    // 使该对象不在json序列化中
    @JsonIgnore
    public boolean isSuccess() {
        return this.status.equals(ResponseEnum.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccess() {
        return new ServerResponse<T>(ResponseEnum.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<T>(ResponseEnum.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<T>(ResponseEnum.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createByERROR(T data) {
        return new ServerResponse<T>(ResponseEnum.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseEnum.SUCCESS.getCode(), msg, data);
    }

    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseEnum.ERROR.getCode(), ResponseEnum.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByError(T data) {
        return new ServerResponse<T>(ResponseEnum.ERROR.getCode(), data);
    }

    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage) {
        return new ServerResponse<T>(ResponseEnum.ERROR.getCode(), errorMessage);
    }

    public static <T> ServerResponse<T> createByCodeMessage(Double code, String errorMessage) {
        return new ServerResponse<T>(code, errorMessage);
    }

    /**
     * 无对应资源的权限
     *
     * @return ServerResponse
     */
    public static ServerResponse noAccessPermissions() {
        return new ServerResponse(ResponseEnum.NO_ACCESS_PERMISSIONS.getCode(), ResponseEnum.NO_ACCESS_PERMISSIONS.getDesc());
    }

    /**
     * 未登录
     *
     * @return ServerResponse
     */
    public static ServerResponse notLogin() {
        return new ServerResponse(ResponseEnum.NOT_LOGIN.getCode(), ResponseEnum.NOT_LOGIN.getDesc());
    }

    /**
     * 用户名或密码错误
     *
     * @return ServerResponse
     */
    public static ServerResponse userNameOrPasswordError() {
        return new ServerResponse(ResponseEnum.USERNAME_PASSWORD_ERROR.getCode(), ResponseEnum.USERNAME_PASSWORD_ERROR.getDesc());
    }

    /**
     * 用户冻结
     *
     * @return ServerResponse
     */
    public static ServerResponse userLock() {
        return new ServerResponse(ResponseEnum.USER_LOCK.getCode(), ResponseEnum.USER_LOCK.getDesc());
    }


    /**
     * 使用response输出JSON
     *
     * @param serverResponse 返回响应
     */
    public static void createResponseEnumJson(ServletResponse response, ServerResponse serverResponse) {
        PrintWriter out = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(JSON.toJSONString(serverResponse));
        } catch (Exception e) {
            log.error("【JSON输出异常】" + e);
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }


}
