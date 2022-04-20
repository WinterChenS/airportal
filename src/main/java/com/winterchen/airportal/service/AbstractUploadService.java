package com.winterchen.airportal.service;

import cn.hutool.core.util.RandomUtil;
import com.winterchen.airportal.entity.FileInfo;
import com.winterchen.airportal.utils.EhcacheUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 10:59
 * @description
 **/
public abstract class AbstractUploadService implements UploadService {

    private final MongoTemplate mongoTemplate;

    protected AbstractUploadService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public List<FileInfo> listUnRemoveAndNeedRemoveInfos() {
        final Criteria criteria = Criteria.where("deleted").is(false);
        criteria.orOperator(Criteria.where("lastDownloadTime").lt(new Date()),Criteria.where("maxGetCount").lt(1));
        Query query = new Query(criteria);
        return mongoTemplate.find(query, FileInfo.class);
    }


    protected String createTakeCode() {
        final String takeCode = RandomUtil.randomNumbers(6);
        final Object o = EhcacheUtil.get(takeCode);
        if (o  == null) {
            return takeCode;
        }
        return createTakeCode();
    }

}