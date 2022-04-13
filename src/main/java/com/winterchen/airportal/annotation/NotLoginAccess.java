package com.winterchen.airportal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chendonghua
 * @version 1.0
 * @date 2020/11/26 1:35 下午
 * @description 免登陆注解
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotLoginAccess {

}
