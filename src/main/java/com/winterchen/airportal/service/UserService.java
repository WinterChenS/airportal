package com.winterchen.airportal.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.UUID;
import cn.hutool.extra.servlet.ServletUtil;
import com.winterchen.airportal.base.ResultCode;
import com.winterchen.airportal.constants.DefaultConstants;
import com.winterchen.airportal.entity.User;
import com.winterchen.airportal.exception.BusinessException;
import com.winterchen.airportal.request.MailSenderRequest;
import com.winterchen.airportal.request.UserLoginRequest;
import com.winterchen.airportal.request.UserRegisterRequest;
import com.winterchen.airportal.response.UserResponse;
import com.winterchen.airportal.utils.EhcacheUtil;
import com.winterchen.airportal.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 15:09
 * @description 用户登录
 **/
@Slf4j
@Service
public class UserService {

    private final MongoTemplate mongoTemplate;

    private final MailService mailService;

    @Value("${airportal.register.enable:true}")
    private Boolean registerEnabled;

    public UserService(MongoTemplate mongoTemplate, MailService mailService) {
        this.mongoTemplate = mongoTemplate;
        this.mailService = mailService;
    }


    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        Assert.isTrue(registerEnabled, "暂未开放注册，敬请期待");
        log.info("用户注册开始, userRegisterRequest:[{}]",userRegisterRequest);
        Query query = new Query(Criteria.where("userName").is(userRegisterRequest.getUserName()).and("deleted").is(false));
        final User user = mongoTemplate.findOne(query, User.class);
        if (user != null) {
            throw BusinessException.newBusinessException(ResultCode.USER_HAS_EXISTED.getCode());
        }
        final User registerUser = toConvertToUser(userRegisterRequest);
        registerUser.setCreatedTime(new Date());
        registerUser.setDeleted(false);
        registerUser.setEnabled(true);
        registerUser.setPass(MD5Util.EncoderByMd5(userRegisterRequest.getPass()));
        String ip = ServletUtil.getClientIP(request, null);
        registerUser.setRegisterIp(ip);
        final User save = mongoTemplate.save(registerUser);
        // 发送邮件
        mailService.sendAttachmentsMail(MailSenderRequest.builder()
                .to(registerUser.getEmail())
                .subject("【airportal】欢迎加入airportal")
                .content("<html>" +
                        "<head></head>" +
                        "<body>" +
                        "<p>尊敬的"+ registerUser.getNickname() +":</p>" +
                        "<p>您好！</p>" +
                        "<p>感谢您的注册，请及时登录系统进行查看。\n</p>" +
                        "<a style=\"margin-left:20px;text-align: center;padding: 4px 16px;border-radius: 5px;color: #ffffff;background-color: #ED7D31;cursor: pointer;\" href=\"http://www.nuqcc.cn/login.html\">立即登陆</a>" +
                        "<p>感谢您使用airportal文件投送平台，祝您愉快！本邮件由系统自动发出，请勿回复！</p>" +
                        "<p style=\"text-align:right;\">winterchen.com</p>" +
                        "<p style=\"text-align:right;\">"+ DateUtil.format(new Date(), "yyyy年MM月dd日 HH:mm:ss") +"</p>" +
                        "</body>" +
                        "</html>")
                .build(), null);
        log.info("用户注册结束, userRegisterRequest:[{}]",userRegisterRequest);
        return toConvertToResponse(save);
    }

    /**
     * 用户登录
     * @param loginRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse login(UserLoginRequest loginRequest, HttpServletRequest request) {
        log.info("用户登录开始, loginRequest:[{}]", loginRequest);
        Query query = new Query(Criteria.where("userName").is(loginRequest.getUserName())
                .and("pass").is(MD5Util.EncoderByMd5(loginRequest.getPass()))
                .and("deleted").is(false));
        final User user = mongoTemplate.findOne(query, User.class);
        if (user == null) {
            throw BusinessException.newBusinessException(ResultCode.USER_LOGIN_ERROR.getCode());
        }
        if (!user.isEnabled()) {
            throw BusinessException.newBusinessException(ResultCode.USER_IS_BLOCK.getCode());
        }
        String ip = ServletUtil.getClientIP(request, null);
        final UserResponse response = toConvertToResponse(user);
        Map<String, Object> map = new HashMap();
        map.put(DefaultConstants.User.USER_ID, user.getId());
        String token = UUID.fastUUID().toString(true);
        response.setToken(token);
        user.setLastLoginIp(ip);
        user.setLastLoginTime(new Date());
        mongoTemplate.save(user);
        EhcacheUtil.put(token, user);
        log.info("用户登录结束, loginRequest:[{}]", loginRequest);
        return response;
    }

    public void logout(HttpServletRequest request) {
        final String token = request.getHeader(DefaultConstants.User.TOKEN_KEY);
        if (StringUtils.hasText(token)) {
            EhcacheUtil.remove(token);
        }
    }

    public User getById(String id) {
        return mongoTemplate.findById(id, User.class);
    }


    private User toConvertToUser(UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) return null;
        User user = new User();
        BeanUtils.copyProperties(userRegisterRequest, user);
        return user;
    }

    private UserResponse toConvertToResponse(User user) {
        if (user == null) return null;
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

}