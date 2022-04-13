package com.winterchen.airportal.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 13:08
 * @description 文件信息
 **/
@Data
@Document(collection = "file_info")
@Builder
public class FileInfo implements Serializable {

    @Id
    private String id;

    private String type;

    private String contentType;

    private String realName;

    private String uploadName;

    private String url;

    private String bucket;

    private long size;

    private String content;

    /**
     * 取件码
     */
    @Indexed(background = true)
    private String takeCode;

    private Integer maxGetCount;

    private String pass;

    private Integer expiresHours;


    private String createdAt;

    private Date createTime;

    private Date lastDownloadTime;

    private Date updateTime;

    private String ip;

    private boolean deleted;

}