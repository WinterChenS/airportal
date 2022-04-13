package com.winterchen.airportal.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/1 11:07
 **/
@ApiModel("邮件发送请求类")
@Data
@Builder
public class MailSenderRequest implements Serializable {

    private static final long serialVersionUID = 148542785170208391L;

    @ApiModelProperty("主键编号")
    private String id;

    @NotBlank(message = "发送的对象不能为空")
    @ApiModelProperty("发送的对象")
    private String to;

    @NotBlank(message = "主题不能为空")
    @ApiModelProperty("主题")
    private String subject;

    @NotBlank(message = "内容不能为空")
    @ApiModelProperty("内容")
    private String content;

}