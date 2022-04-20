package com.winterchen.airportal.service;

import com.winterchen.airportal.entity.FileInfo;
import com.winterchen.airportal.response.ShareResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 15:28
 * @description 上传服务接口
 **/
public interface UploadService {

    /**
     * 上传
     * @param content 内容
     * @param pass 密码
     * @param expire 保留时间：小时
     * @param maxGetCount 最大领取数量
     * @return
     */
    ShareResponse upload(Object content, String pass, Integer expire, Integer maxGetCount);


    /**
     * 获取
     * @param takeCode 区间码
     * @param pass 密码
     * @return
     */
    String get(String takeCode, String pass, HttpServletResponse response);


    void remove(FileInfo fileInfo);

}