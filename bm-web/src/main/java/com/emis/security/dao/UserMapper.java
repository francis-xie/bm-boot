package com.emis.security.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emis.security.model.PermissionModel;
import com.emis.security.model.RoleModel;
import com.emis.security.model.UserModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description: UserMapper
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
//加入Spring管理
@Repository
//@Mapper表示这是一个Mybatis Mapper接口。
@Mapper
//public interface UserMapper extends BaseMapper<UserModel> {
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param userName 用户名称
     * @return UserModel
     */
    //UserModel selectUserModelByUserName(@Param("userName") String userName);
    @Select(" select user_id,account,password,user_name,phone,email,sex,id_card,state,create_time,update_time from user " +
            "where state = 0 and (phone = #{userName} or email = #{userName} or account = #{userName}) ")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "roleModels", javaType = List.class, column = "user_id", many = @Many(select = "com.emis.security.dao.UserMapper.findRoleByUserId")),
            @Result(property = "permissionModels", javaType = List.class, column = "user_id", many = @Many(select = "com.emis.security.dao.UserMapper.findPermissionByRoleId"))
    })
    UserModel selectUserModelByUserName(String userName);

    @Select(" select r.role_id,r.role_name,r.role_title,r.description,r.create_time,r.update_time,r.state from user_role ur " +
            "left join role r on ur.role_id = r.role_id where r.state = 0 and ur.user_id= #{user_id} ")
    RoleModel findRoleByUserId(int user_id);

    @Select(" select p.permission_id,p.permission_name,p.permission_value,p.permission_type,p.permission_state,p.superior_id from permission p " +
            "left join ( select rp.permission_id from role_permission rp where EXISTS" +
            " ( select ur.role_id from user_role ur where ur.role_id = rp.role_id and user_id = #{user_id} ) " +
            "GROUP BY rp.permission_id ) cc ON cc.permission_id = p.permission_id and p.permission_state = 0 ")
    PermissionModel findPermissionByRoleId(int user_id);
}
