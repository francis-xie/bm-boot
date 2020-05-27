package com.emis.security.controller;

import com.emis.security.model.UserModel;
import com.emis.security.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@RestController
@RequestMapping("/user/")
@Api(tags = "用户模块")
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "list")
    @PreAuthorize("hasPermission(null ,'system_manage')")
    @ApiOperation(value = "用户列表")
    public UserModel userList() {
        return userService.selectUserModelByUserName("187123456789");
    }
}
