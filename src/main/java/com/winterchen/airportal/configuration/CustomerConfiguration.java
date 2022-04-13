package com.winterchen.airportal.configuration;

import com.winterchen.airportal.interceptors.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 16:32
 * @description 用户配置
 **/
@Slf4j
@Configuration
public class CustomerConfiguration  implements WebMvcConfigurer {


    private static final String[] EXCLUDE_PATHS = new String[] {

            //静态资源
            "/views/**","/zui/**" , "/user/login" ,

            //swagger相关静态资源
            "swagger-ui.html" , "/swagger-resources/**" , "/swagger-resources" , "/v2/**" ,

            //knife4j
            "/doc.html" , "/webjars/**"

    };

    @Bean
    public LoginInterceptor loginInterceptor() {
        log.info("loginInterceptor init");
        return new LoginInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor()).addPathPatterns("/**").excludePathPatterns(EXCLUDE_PATHS);
    }

//    @Bean
//    MongoTransactionManager transactionManager(MongoDatabaseFactory factory){
//        return new MongoTransactionManager(factory);
//    }



}