package com.winterchen.airportal.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.winterchen.airportal.base.ResultCode;
import com.winterchen.airportal.entity.FileInfo;
import com.winterchen.airportal.entity.UploadRecord;
import com.winterchen.airportal.enums.UploadType;
import com.winterchen.airportal.exception.BusinessException;
import com.winterchen.airportal.response.ShareResponse;
import com.winterchen.airportal.utils.EhcacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 14:24
 * @description 文字上传服务
 **/
@Slf4j
@Service("contentUploadService")
public class ContentUploadService extends AbstractUploadService {

    private final MongoTemplate mongoTemplate;

    public ContentUploadService(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareResponse upload(Object target, String pass, Integer expire, Integer maxGetCount) {
        Assert.notNull(target, "文件不能为空");
        Assert.isTrue(target instanceof String, "错误的上传类型");
        log.info("start content upload, target: [{}]", target);

        String content = (String) target;

        final String takeCode = createTakeCode();
        final Date now = new Date();
        //文件上传
        try {
            final FileInfo fileInfo = FileInfo.builder()
                    .size(content.length())
                    .contentType(String.class.getTypeName())
                    .createTime(now)
                    .deleted(false)
                    .expiresHours(expire)
                    .content(content)
                    .takeCode(takeCode)
                    .maxGetCount(maxGetCount)
                    .updateTime(now)
                    .lastDownloadTime(DateUtil.offsetHour(now, expire))
                    .type(UploadType.STRING.name())
                    .pass(pass)
                    .build();
            final FileInfo info = mongoTemplate.save(fileInfo);
            //上传记录
            UploadRecord uploadRecord = UploadRecord.builder()
                    .type(UploadType.STRING.name())
                    .targetId(info.getId())
                    .takeCode(takeCode)
                    .createTime(now)
                    .build();
            mongoTemplate.save(uploadRecord);
            //将取件码写入到缓存，防止被刷库
            EhcacheUtil.put(takeCode, info);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.newBusinessException(ResultCode.KNOWN_ERROR.getCode());
        }

        log.info("end content upload target: [{}]", target);
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
            Assert.isTrue(false, "文件不存在");
        }

        Query query = new Query(Criteria.where("takeCode").is(takeCode).and("deleted").is(false));
        final FileInfo fileInfo = mongoTemplate.findOne(query, FileInfo.class);
        Assert.notNull(fileInfo, "文件不存在");

        fileInfo.setMaxGetCount(fileInfo.getMaxGetCount() - 1);
        mongoTemplate.save(fileInfo);
        EhcacheUtil.put(takeCode, fileInfo);
        return fileInfo.getContent();
    }

    @Override
    public void remove(FileInfo fileInfo) {
        if (fileInfo == null) return;
        log.info("清除失效的内容开始, fileInfo: [{}]",fileInfo);
        fileInfo.setDeleted(true);
        fileInfo.setUpdateTime(new Date());
        mongoTemplate.save(fileInfo);
        log.info("清除失效的内容结束, fileInfo: [{}]",fileInfo);
    }
}