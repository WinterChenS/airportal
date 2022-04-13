package com.winterchen.airportal.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 15:06
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    @ApiModelProperty("用户编号")
    private String id;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("token")
    private String token;

}