package com.winterchen.airportal.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 15:05
 **/
@Data
@ApiModel("登录请求")
public class UserLoginRequest {

    @NotBlank(message = "登录用户名不能为空")
    @ApiModelProperty("登录用户名")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty("密码")
    private String pass;


}