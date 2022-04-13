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
 * @date 2022/4/12 14:32
 * @description 文件下载记录
 **/
@Data
@Builder
@Document("download_record")
public class DownloadRecord {

    @Id
    private String id;

    private String type;

    @Indexed(background = true)
    private String targetId;

    private String ip;

    private Date createTime;

}