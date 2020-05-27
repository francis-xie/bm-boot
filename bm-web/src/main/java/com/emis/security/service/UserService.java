package com.emis.security.service;

import com.emis.security.model.UserModel;

/**
 * @Description:
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
public interface UserService {

    /**
     * 根据用户名称查询用户信息
     *
     * @param userName 用户名称
     * @return UserModel
     */
    UserModel selectUserModelByUserName(String userName);
}
