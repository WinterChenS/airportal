package com.winterchen.airportal.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/1 12:58
 * @description 邮件
 **/
@Data
@Document(collection = "mail")
public class Mail implements Serializable {

    private static final long serialVersionUID = 7259840335580214408L;

    @Id
    private String id;

    private String from;

    private String to;

    private String subject;

    private String content;

    private String status;

    private Date createTime;

    private boolean deleted;

}