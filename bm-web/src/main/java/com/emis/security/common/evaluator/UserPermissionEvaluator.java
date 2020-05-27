package com.emis.security.common.evaluator;

import com.emis.security.model.PermissionModel;
import com.emis.security.model.UserModel;
import com.emis.security.service.UserService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 自定义权限注解验证
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/

@Component
public class UserPermissionEvaluator implements PermissionEvaluator {


    @Resource
    UserService userService;

    public UserPermissionEvaluator() {
    }

    /**
     * hasPermission鉴权方法
     * 这里仅仅判断PreAuthorize注解中的permission
     * 实际中可以根据业务需求设计数据库通过targetUrl和permission做更复杂鉴权
     *
     * @param authentication 用户身份
     * @param targetUrl      请求路径
     * @param permission     请求路径权限
     * @return boolean 是否通过
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetUrl, Object permission) {
        // 获取用户信息
        UserModel userModel = (UserModel) authentication.getPrincipal();
        // 获取用户对应角色的权限(因为SQL中已经GROUP BY了，所以返回的list是不重复的)
        List<PermissionModel> permissionModels = userService.selectUserModelByUserName(userModel.getUserName()).getPermissionModels();
        List<String> rolePermissions = new ArrayList<>();
        for (PermissionModel permissionModel : permissionModels) {
            rolePermissions.add(permissionModel.getPermissionValue());
        }
        // 权限对比
        return rolePermissions.contains(permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
