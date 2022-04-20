package com.winterchen.airportal.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/19 13:20
 **/
@ApiModel("文件信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfoResponse {

    private String type;

    private String contentType;

    private String realName;

    private long size;


}