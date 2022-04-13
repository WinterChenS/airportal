package com.winterchen.airportal.factory;

import com.winterchen.airportal.base.ResultCode;
import com.winterchen.airportal.enums.UploadType;
import com.winterchen.airportal.exception.BusinessException;
import com.winterchen.airportal.service.UploadService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 10:25
 * @description 上传工厂类
 **/
@Component
public class UploadProviderFactory implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static UploadService getProvider(String type) {
        final UploadType uploadType = UploadType.valueOf(type);
        switch (uploadType) {
            case FILE:
                return context.getBean("fileUploadService", UploadService.class);
            case STRING:
                return context.getBean("contentUploadService", UploadService.class);
            case DIR:
                throw BusinessException.newBusinessException(ResultCode.PARAM_IS_INVALID.getCode());
        }
        return null;
    }

}