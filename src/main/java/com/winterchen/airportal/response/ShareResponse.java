package com.winterchen.airportal.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 14:31
 **/
@Data
@Builder
@ApiModel("文件分享响应类")
public class ShareResponse {

    @ApiModelProperty("取件码")
    private String takeCode;

    private String url;

}