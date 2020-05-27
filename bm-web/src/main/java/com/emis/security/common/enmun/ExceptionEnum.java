package com.emis.security.common.enmun;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @Description: 异常信息枚举
 * @Author: LiuRunYong
 * @Date: 2020/4/1
 **/

@Getter
@AllArgsConstructor
public enum ExceptionEnum {

    /**
     * Mysql新增异常
     */
    MYSQL_INSERT_EXCEPTION(10, "Mysql新增异常"),
    /**
     * Mysql修改异常
     */
    MYSQL_UPDATE_EXCEPTION(11, "Mysql修改异常"),
    /**
     * Mysql删除异常
     */
    MYSQL_DELETE_EXCEPTION(12, "Mysql删除异常");


    private Integer code;

    private String message;
}
