package com.emis.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @Description: UserModel
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "用户Bean")
@ToString(exclude = "password")
public class UserModel implements Serializable {

    @ApiModelProperty(value = "主键(新增?,更新*)")
    private Integer userId;

    @ApiModelProperty(value = "账号", hidden = true)
    private String account;

    @ApiModelProperty(value = "用户名称")
    private String userName;

    @ApiModelProperty(value = "密码", hidden = true)
    @JsonIgnore
    private String password;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "性别(0:男,1:女)")
    private Integer sex;

    @ApiModelProperty(value = "用户身份证号")
    private Integer idCard;

    @ApiModelProperty(value = "用户状态(0:可用,1:不可用,2:暂时锁定)")
    private Integer state;

    @ApiModelProperty(value = "创建时间", hidden = true)
    private String createTime;

    @ApiModelProperty(value = "修改时间", hidden = true)
    private String updateTime;

    @ApiModelProperty(value = "角色", hidden = true)
    private Set<GrantedAuthority> authorities;

    @ApiModelProperty(value = "用户角色", hidden = true)
    private List<RoleModel> roleModels;

    @ApiModelProperty(value = "用户权限", hidden = true)
    private List<PermissionModel> permissionModels;
}
