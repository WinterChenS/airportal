package com.winterchen.airportal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/1 13:09
 * @description 邮件状态
 **/
@Getter
@AllArgsConstructor
public enum MailStatusEnum {

    DRAFT("草稿"),
    IS_SEND("已发送"),
    FAILED("发送失败"),
    ;

    private String desc;

}