package com.winterchen.airportal.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/6/30 14:29
 **/
@Data
@ApiModel("内容分享请求")
public class ContentShareRequest implements Serializable {

    private static final long serialVersionUID = -4872673830654210952L;

    @ApiModelProperty(value = "内容对象", required = true)
    private String content;
    @ApiModelProperty(value = "pass", required = true)
    @ApiParam(value = "pass",required = false)
    private String pass;
    @ApiModelProperty(value = "保留时间(hours)", required = true)
    private Integer expire;
    @ApiModelProperty(value = "最大下载次数", required = true)
    private Integer maxGetCount;

}