package com.emis.security.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Description: UserRoleModel
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "用户角色Bean")
public class UserRoleModel implements Serializable {

    @ApiModelProperty(value = "主键", hidden = true)
    private Integer userRoleId;

    @ApiModelProperty(value = "用户主键", hidden = true)
    private Integer userId;

    @ApiModelProperty(value = "角色主键", hidden = true)
    private Integer roleId;
}
