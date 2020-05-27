package com.emis.security.impl;

import com.emis.security.dao.UserMapper;
import com.emis.security.model.UserModel;
import com.emis.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description:
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserModel selectUserModelByUserName(String userName) {
        return userMapper.selectUserModelByUserName(userName);
    }
}
