package com.winterchen.airportal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 14:54
 * @description 用户
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("user")
public class User implements Serializable {

    private static final long serialVersionUID = -75071000283542992L;

    @Id
    private String id;

    
    private String role;

    private String nickname;

    private String userName;

    private String pass;

    private String email;

    private boolean enabled = true;

    private Date createdTime;

    private String registerIp;

    private Date lastLoginTime;

    private String lastLoginIp;

    private Date updateTime;


    private boolean deleted;

    public String getUserName() {
        return StringUtils.isBlank(userName) ? getEmail() : userName;
    }

}