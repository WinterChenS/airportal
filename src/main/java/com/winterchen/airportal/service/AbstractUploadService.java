package com.winterchen.airportal.service;

import cn.hutool.core.util.RandomUtil;
import com.winterchen.airportal.utils.EhcacheUtil;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 10:59
 * @description
 **/
public abstract class AbstractUploadService implements UploadService {


    protected String createTakeCode() {
        final String takeCode = RandomUtil.randomNumbers(6);
        final Object o = EhcacheUtil.get(takeCode);
        if (o  == null) {
            return takeCode;
        }
        return createTakeCode();
    }

}