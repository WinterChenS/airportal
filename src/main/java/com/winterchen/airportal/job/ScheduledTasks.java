package com.winterchen.airportal.job;

import com.winterchen.airportal.service.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/20 9:33
 * @description 定时任务
 **/
@Slf4j
@Component
public class ScheduledTasks {

    private final ShareService shareService;

    public ScheduledTasks(ShareService shareService) {
        this.shareService = shareService;
    }

    @Scheduled(fixedDelay = 60000)
    public void toTasks() {
        log.info("start find failure info to remove.");
        shareService.checkValidateAndRemove();
        log.info("end find failure info to remove.");
    }

}