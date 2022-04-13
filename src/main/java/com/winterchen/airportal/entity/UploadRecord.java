package com.winterchen.airportal.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 14:37
 * @description 文件上传记录
 **/
@Data
@Builder
@Document(collection = "upload_record")
public class UploadRecord {

    @Id
    private String id;

    private String type;

    @Indexed(background = true)
    private String targetId;

    private String takeCode;

    private String createdAt;

    private Date createTime;

}