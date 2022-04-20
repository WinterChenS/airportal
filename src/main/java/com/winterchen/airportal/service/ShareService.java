package com.winterchen.airportal.service;

import com.winterchen.airportal.entity.FileInfo;
import com.winterchen.airportal.factory.UploadProviderFactory;
import com.winterchen.airportal.response.FileInfoResponse;
import com.winterchen.airportal.response.ShareResponse;
import com.winterchen.airportal.utils.EhcacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 10:36
 * @description 分享资源服务
 **/
@Slf4j
@Service
public class ShareService {

    private final FileUploadService fileUploadService;

    public ShareService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }


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
     * 检查分享的文件是否有效，如果无效就将分享的文件状态置为失效即可
     */
    public void checkValidateAndRemove() {
        final List<FileInfo> fileInfos = fileUploadService.listUnRemoveAndNeedRemoveInfos();
        if (CollectionUtils.isEmpty(fileInfos)) {
            return;
        }
        fileInfos.forEach(info -> {
            final UploadService provider = UploadProviderFactory.getProvider(info.getType());
            provider.remove(info);
        });
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

    /**
     * 获取基本信息
     * @param takeCode
     * @return
     */
    public FileInfoResponse findFileInfo(String takeCode, String pass) {
        final FileInfo fileInfo = (FileInfo) EhcacheUtil.get(takeCode);
        Assert.notNull(fileInfo, "文件不存在");
        final UploadService provider = UploadProviderFactory.getProvider(fileInfo.getType());
        Assert.isTrue(provider.check(takeCode, pass));
        return toConvertToResponse(fileInfo);
    }





    private FileInfoResponse toConvertToResponse(FileInfo fileInfo) {
        if (fileInfo == null) return null;
        FileInfoResponse response = new FileInfoResponse();
        BeanUtils.copyProperties(fileInfo, response);
        return response;
    }





}