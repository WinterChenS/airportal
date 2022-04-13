package com.winterchen.airportal.service;

import com.winterchen.airportal.entity.FileInfo;
import com.winterchen.airportal.factory.UploadProviderFactory;
import com.winterchen.airportal.response.ShareResponse;
import com.winterchen.airportal.utils.EhcacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 10:36
 * @description 分享资源服务
 **/
@Slf4j
@Service
public class ShareService {

    /**
     * 分享
     * @param content
     * @param pass
     * @param expire
     * @param maxGetCount
     * @param type
     * @return
     */
    public ShareResponse upload(Object content, String pass, Integer expire, Integer maxGetCount, String type) {
        final UploadService provider = UploadProviderFactory.getProvider(type);
        return provider.upload(content,pass,expire,maxGetCount);
    }

    /**
     * 检查提取是否需要密码
     * @param takeCode
     * @return
     */
    public boolean checkNeedPass(String takeCode) {
        final FileInfo fileInfo = (FileInfo) EhcacheUtil.get(takeCode);
        Assert.notNull(fileInfo, "文件不存在");
        return StringUtils.isNotBlank(fileInfo.getPass());
    }

    /**
     * 获取
     * @param takeCode
     * @param pass
     * @param response
     * @return
     */
    public String get(String takeCode, String pass, HttpServletResponse response) {
        final FileInfo fileInfo = (FileInfo) EhcacheUtil.get(takeCode);
        Assert.notNull(fileInfo, "文件不存在");
        final UploadService provider = UploadProviderFactory.getProvider(fileInfo.getType());
        return provider.get(takeCode, pass, response);
    }




}