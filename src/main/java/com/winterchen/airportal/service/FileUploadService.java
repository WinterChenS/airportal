package com.winterchen.airportal.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.winterchen.airportal.base.FileUploadResult;
import com.winterchen.airportal.base.MultipartUploadCreate;
import com.winterchen.airportal.base.ResultCode;
import com.winterchen.airportal.entity.FileInfo;
import com.winterchen.airportal.entity.UploadRecord;
import com.winterchen.airportal.entity.User;
import com.winterchen.airportal.enums.UploadType;
import com.winterchen.airportal.exception.BusinessException;
import com.winterchen.airportal.request.CompleteMultipartUploadRequest;
import com.winterchen.airportal.request.MultipartUploadCreateRequest;
import com.winterchen.airportal.response.MultipartUploadCreateResponse;
import com.winterchen.airportal.response.ShareResponse;
import com.winterchen.airportal.utils.EhcacheUtil;
import com.winterchen.airportal.utils.MinioHelper;
import com.winterchen.airportal.utils.UserThreadLocal;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.ServerException;
import io.minio.messages.Part;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
        super(mongoTemplate);
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
                .url(minioHelper.minioProperties.getDownloadUri() + "/download/" + takeCode + "   粘贴到浏览器打开")
                .build();
    }


    /**
     * 创建分片上传
     * @param createRequest
     * @return
     */
    public MultipartUploadCreateResponse createMultipartUpload(MultipartUploadCreateRequest createRequest) {
        log.info("创建分片上传开始, createRequest: [{}]", createRequest);
        MultipartUploadCreateResponse response = new MultipartUploadCreateResponse();
        response.setChunks(new LinkedList<>());
        final MultipartUploadCreate uploadCreate = MultipartUploadCreate.builder()
                .bucketName(minioHelper.minioProperties.getBucketName())
                .objectName(createRequest.getFileName())
                .build();
        final CreateMultipartUploadResponse uploadId = minioHelper.uploadId(uploadCreate);
        uploadCreate.setUploadId(uploadId.result().uploadId());
        response.setUploadId(uploadCreate.getUploadId());
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("uploadId", uploadId.result().uploadId());
        for (int i = 0; i < createRequest.getChunkSize(); i++) {
            reqParams.put("partNumber", String.valueOf(i));
            String presignedObjectUrl = minioHelper.getPresignedObjectUrl(uploadCreate.getBucketName(), uploadCreate.getObjectName(), reqParams);
            if (StringUtils.isNotBlank(minioHelper.minioProperties.getPath())) {//如果线上环境配置了域名解析，可以进行替换
                presignedObjectUrl = presignedObjectUrl.replace(minioHelper.minioProperties.getEndpoint(), minioHelper.minioProperties.getPath());
            }
            MultipartUploadCreateResponse.UploadCreateItem item = new MultipartUploadCreateResponse.UploadCreateItem();
            item.setPartNumber(i);
            item.setUploadUrl(presignedObjectUrl);
            response.getChunks().add(item);
        }
        log.info("创建分片上传结束, createRequest: [{}]", createRequest);
        return response;
    }

    /**
     * 分片合并
     * @param uploadRequest
     */
    public ShareResponse completeMultipartUpload(CompleteMultipartUploadRequest uploadRequest) {
        log.info("文件合并开始, uploadRequest: [{}]", uploadRequest);
        try {
            final String takeCode = createTakeCode();
            final User user = UserThreadLocal.getUser();
            final ListPartsResponse listMultipart = minioHelper.listMultipart(MultipartUploadCreate.builder()
                    .bucketName(minioHelper.minioProperties.getBucketName())
                    .objectName(uploadRequest.getFileName())
                    .maxParts(uploadRequest.getChunkSize() + 10)
                    .uploadId(uploadRequest.getUploadId())
                    .partNumberMarker(0)
                    .build());
            final ObjectWriteResponse objectWriteResponse = minioHelper.completeMultipartUpload(MultipartUploadCreate.builder()
                    .bucketName(minioHelper.minioProperties.getBucketName())
                    .uploadId(uploadRequest.getUploadId())
                    .objectName(uploadRequest.getFileName())
                    .parts(listMultipart.result().partList().toArray(new Part[]{}))
                    .build());
            final Date now = new Date();
            final String url = minioHelper.minioProperties.getDownloadUri() + takeCode  + "   粘贴到浏览器打开";
            final FileInfo fileInfo = FileInfo.builder()
                    .bucket(minioHelper.minioProperties.getBucketName())
                    .size(uploadRequest.getFileSize())
                    .contentType(uploadRequest.getContentType())
                    .createTime(now)
                    .createdAt(user.getId())
                    .deleted(false)
                    .expiresHours(uploadRequest.getExpire())
                    .takeCode(takeCode)
                    .maxGetCount(uploadRequest.getMaxGetCount())
                    .realName(uploadRequest.getFileName())
                    .uploadName(uploadRequest.getFileName())
                    .url(objectWriteResponse.region())
                    .updateTime(now)
                    .lastDownloadTime(DateUtil.offsetHour(now, uploadRequest.getExpire()))
                    .type(UploadType.FILE.name())
                    .pass(uploadRequest.getPass())
                    .build();
            final FileInfo info = mongoTemplate.save(fileInfo);
            //上传记录
            UploadRecord uploadRecord = UploadRecord.builder()
                    .type(UploadType.FILE.name())
                    .targetId(info.getId())
                    .takeCode(takeCode)
                    .createdAt(user.getId())
                    .createTime(now)
                    .build();
            mongoTemplate.save(uploadRecord);
            //将取件码写入到缓存，防止被刷库
            EhcacheUtil.put(takeCode, info);
            return ShareResponse.builder()
                    .takeCode(takeCode)
                    .url(url)
                    .build();
        } catch (Exception e) {
            log.error("合并分片失败", e);
        }
        log.info("文件合并结束, uploadRequest: [{}]", uploadRequest);
        return null;
    }





    @Override
    @Transactional(rollbackFor = Exception.class)
    public String get(String takeCode, String pass, HttpServletResponse response) {
        //需要从缓存中查询校验码，如果不存在就不进行后续的操作，防止被刷库,同时校验密码
        FileInfo fileInfo = checkFileInfo(takeCode, pass);

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





    private FileInfo checkFileInfo(String takeCode, String pass) {
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
        return fileInfo;
    }

    @Override
    public boolean check(String takeCode, String pass) {
        return checkFileInfo(takeCode, pass) != null;
    }

    @Override
    public void remove(FileInfo fileInfo) {
        if (fileInfo == null) return;
        log.info("清除失效的文件开始, fileInfo: [{}]",fileInfo);
        fileInfo.setDeleted(true);
        fileInfo.setUpdateTime(new Date());
        try {
            minioHelper.removeFile(fileInfo.getUploadName());
        } catch (Exception e) {
            log.error("删除文件失败", e);
        }
        mongoTemplate.save(fileInfo);
        log.info("清除失效的文件结束, fileInfo: [{}]",fileInfo);
    }
}