package com.winterchen.airportal.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 15:00
 **/
@ApiModel("用户注册请求类")
@Data
public class UserRegisterRequest {

    @NotBlank(message = "昵称不能为空")
    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("登录用户名")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty("密码")
    private String pass;

    @NotBlank(message = "邮箱地址不能为空")
    @ApiModelProperty("邮箱地址")
    private String email;

    public String getUserName() {
        return StringUtils.isBlank(userName) ? getEmail() : userName;
    }
}