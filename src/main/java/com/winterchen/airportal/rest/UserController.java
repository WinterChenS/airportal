package com.winterchen.airportal.rest;

import com.winterchen.airportal.annotation.NotLoginAccess;
import com.winterchen.airportal.request.UserLoginRequest;
import com.winterchen.airportal.request.UserRegisterRequest;
import com.winterchen.airportal.response.UserResponse;
import com.winterchen.airportal.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 16:50
 **/
@Api(tags = "用户")
@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    @NotLoginAccess
    @ApiOperation("注册")
    @PostMapping("/register")
    public UserResponse register(
            @RequestBody
            @Validated
            UserRegisterRequest userRegisterRequest,
            HttpServletRequest request
    ) {
        return userService.register(userRegisterRequest, request);
    }

    @NotLoginAccess
    @ApiOperation("登录")
    @PostMapping("/login")
    public UserResponse login(
            @RequestBody
            @Validated
            UserLoginRequest loginRequest,
            HttpServletRequest request
    ) {
        return userService.login(loginRequest, request);
    }

    @ApiOperation("注销")
    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        userService.logout(request);
    }


}