package com.winterchen.airportal;

import com.winterchen.airportal.factory.YamlPropertySourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@PropertySource(value = {"file:/home/winterchen/conf/airportal.yml"}, encoding = "UTF-8", ignoreResourceNotFound = true, factory = YamlPropertySourceFactory.class)
public class AirportalApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirportalApplication.class, args);
    }

}
