package com.winterchen.airportal.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.winterchen.airportal.base.FileUploadResult;
import com.winterchen.airportal.base.ResultCode;
import com.winterchen.airportal.entity.FileInfo;
import com.winterchen.airportal.entity.UploadRecord;
import com.winterchen.airportal.enums.UploadType;
import com.winterchen.airportal.exception.BusinessException;
import com.winterchen.airportal.response.ShareResponse;
import com.winterchen.airportal.utils.EhcacheUtil;
import com.winterchen.airportal.utils.MinioHelper;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 14:24
 * @description 文件上传服务
 **/
@Slf4j
@Service("fileUploadService")
public class FileUploadService extends AbstractUploadService {

    private final MongoTemplate mongoTemplate;

    private final MinioHelper minioHelper;

    public FileUploadService(MongoTemplate mongoTemplate, MinioHelper minioHelper) {
        this.mongoTemplate = mongoTemplate;
        this.minioHelper = minioHelper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareResponse upload(Object target, String pass, Integer expire, Integer maxGetCount) {
        Assert.notNull(target, "文件不能为空");
        Assert.isTrue(target instanceof MultipartFile, "错误的上传类型");
        log.info("start file upload");

        MultipartFile multipartFile = (MultipartFile) target;

        final String takeCode = createTakeCode();
        final Date now = new Date();
        //文件上传
        try {
            final FileUploadResult fileUploadResult = minioHelper.uploadFile(multipartFile);
            final FileInfo fileInfo = FileInfo.builder()
                    .bucket(fileUploadResult.getBucket())
                    .size(fileUploadResult.getSize())
                    .contentType(multipartFile.getContentType())
                    .createTime(now)
                    .deleted(false)
                    .expiresHours(expire)
                    .takeCode(takeCode)
                    .maxGetCount(maxGetCount)
                    .realName(fileUploadResult.getRealName())
                    .uploadName(fileUploadResult.getUploadName())
                    .url(fileUploadResult.getUrl())
                    .updateTime(now)
                    .lastDownloadTime(DateUtil.offsetHour(now, expire))
                    .type(UploadType.FILE.name())
                    .pass(pass)
                    .build();
            final FileInfo info = mongoTemplate.save(fileInfo);
            //上传记录
            UploadRecord uploadRecord = UploadRecord.builder()
                    .type(UploadType.FILE.name())
                    .targetId(info.getId())
                    .takeCode(takeCode)
                    .createTime(now)
                    .build();
            mongoTemplate.save(uploadRecord);
            //将取件码写入到缓存，防止被刷库
            EhcacheUtil.put(takeCode, info);
        } catch (IOException e) {
            log.error("file upload error.", e);
            throw BusinessException.newBusinessException(ResultCode.FILE_IO_ERROR.getCode());
        } catch (ServerException e) {
            log.error("minio server error.", e);
            throw BusinessException.newBusinessException(ResultCode.MINIO_SERVER_ERROR.getCode());
        } catch (InsufficientDataException e) {
            log.error("insufficient data throw exception", e);
            throw BusinessException.newBusinessException(ResultCode.MINIO_INSUFFICIENT_DATA.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.newBusinessException(ResultCode.KNOWN_ERROR.getCode());
        }

        log.info("end file upload");
        return ShareResponse.builder()
                .takeCode(takeCode)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String get(String takeCode, String pass, HttpServletResponse response) {
        //需要从缓存中查询校验码，如果不存在就不进行后续的操作，防止被刷库,同时校验密码
        final FileInfo fileInfoInCache = (FileInfo) EhcacheUtil.get(takeCode);
        Assert.notNull(fileInfoInCache, "文件不存在");
        if (StringUtils.isNotBlank(fileInfoInCache.getPass())) {
            Assert.notBlank(pass, "密码错误");
            Assert.isTrue(pass.equals(fileInfoInCache.getPass()), "密码错误");
        }
        if (fileInfoInCache.getLastDownloadTime().getTime() < new Date().getTime() || fileInfoInCache.getMaxGetCount() < 1) {
            fileInfoInCache.setDeleted(true);
            mongoTemplate.save(fileInfoInCache);
            EhcacheUtil.remove(takeCode);
            try {
                minioHelper.removeFile(fileInfoInCache.getUploadName());
            } catch (Exception e) {
                log.error("删除文件失败", e);
            }
            Assert.isTrue(false, "文件不存在");
        }

        Query query = new Query(Criteria.where("takeCode").is(takeCode).and("deleted").is(false));
        final FileInfo fileInfo = mongoTemplate.findOne(query, FileInfo.class);
        Assert.notNull(fileInfo, "文件不存在");

        //download
        try {
            minioHelper.download(response, fileInfo.getUploadName(), fileInfo.getRealName());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.newBusinessException(ResultCode.KNOWN_ERROR.getCode(), "文件下载失败");
        }
        fileInfo.setMaxGetCount(fileInfo.getMaxGetCount() - 1);
        mongoTemplate.save(fileInfo);
        EhcacheUtil.put(takeCode, fileInfo);
        return null;
    }
}