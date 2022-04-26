package com.winterchen.airportal.service;

import cn.hutool.core.lang.Assert;
import com.winterchen.airportal.entity.Mail;
import com.winterchen.airportal.enums.MailStatusEnum;
import com.winterchen.airportal.request.MailSenderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/1 11:05
 * @description 邮件服务
 **/
@Slf4j
@Service
public class MailService {


    private final JavaMailSender mailSender;



    @Value("${spring.mail.username}")
    private String from;


    private final MongoTemplate mongoTemplate;

    @Value("${spring.mail.enable:false}")
    private boolean enable;

    public MailService(JavaMailSender mailSender, MongoTemplate mongoTemplate) {
        this.mailSender = mailSender;
        this.mongoTemplate = mongoTemplate;
    }

    public void sendAttachmentsMail(MailSenderRequest mailSenderRequest, MultipartFile multipartFile){
        if (!enable) {
            return;
        }
        log.info("发送邮件开始, mailSenderRequest: [{}]", mailSenderRequest);
        MimeMessage message = mailSender.createMimeMessage();
        final Mail mail = toConvertToBean(mailSenderRequest);
        mail.setFrom(from);
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(mailSenderRequest.getTo());
            helper.setSubject(mailSenderRequest.getSubject());
            helper.setText(mailSenderRequest.getContent(), true);

            if (multipartFile != null && !multipartFile.isEmpty()) {
                helper.addAttachment(multipartFile.getOriginalFilename(), multipartFile);
            }

            mailSender.send(message);
            log.info("邮件已经发送");
            mail.setStatus(MailStatusEnum.IS_SEND.name());
            saveMail(mail);
        } catch (MessagingException e) {
            log.error("发送邮件失败, mailSenderRequest: [{}]", mailSenderRequest);
            mail.setStatus(MailStatusEnum.FAILED.name());
            saveMail(mail);
            Assert.isTrue(false, "邮件发送失败");
        }
        log.info("发送邮件结束, mailSenderRequest: [{}]", mailSenderRequest);
    }

    /**
     * 保存到草稿箱
     * @param mailSenderRequest
     */
    public void saveDraft(MailSenderRequest mailSenderRequest) {
        log.info("保存邮件到草稿箱开始, mailSenderRequest: [{}]", mailSenderRequest);
        final Mail Mail = toConvertToBean(mailSenderRequest);
        Mail.setStatus(MailStatusEnum.DRAFT.name());
        saveMail(Mail);
        log.info("保存邮件到草稿箱结束, mailSenderRequest: [{}]", mailSenderRequest);
    }

    /**
     * 保存邮件
     * @param Mail
     */
    public void saveMail(Mail Mail) {
        log.info("保存邮件开始, Mail: [{}]", Mail);
        final Date now = new Date();
        Mail.setCreateTime(now);
        Mail.setDeleted(false);
        mongoTemplate.save(Mail);
        log.info("保存邮件结束, Mail: [{}]", Mail);
    }

    public void deleteMail(String id) {
        log.info("删除邮件开始, id:[{}]", id);
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("deleted", true);
        mongoTemplate.updateFirst(query, update, Mail.class);
        log.info("删除邮件结束, id:[{}]", id);
    }



    private Mail toConvertToBean(MailSenderRequest mailSenderRequest) {
        if (mailSenderRequest == null) {
            return null;
        }
        Mail result = new Mail();
        BeanUtils.copyProperties(mailSenderRequest, result);
        return result;
    }




}