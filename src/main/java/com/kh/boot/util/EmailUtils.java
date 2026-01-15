package com.kh.boot.util;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import com.kh.boot.service.EmailRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailUtils {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Autowired
    private EmailRecordService emailRecordService;

    /**
     * Send Simple Email
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        boolean success = false;
        String failReason = null;
        try {
            if (mailSender == null) {
                log.warn("JavaMailSender is not configured. Email to {} skipped.", to);
                failReason = "JavaMailSender not configured";
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            if (from != null && !from.isEmpty()) {
                message.setFrom(from);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Email sent to {}", to);
            success = true;
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            failReason = e.getMessage();
            throw new RuntimeException("Email sending failed", e);
        } finally {
            if (emailRecordService != null) {
                emailRecordService.saveRecord(to, subject, content, success, failReason);
            }
        }
    }

    /**
     * Send HTML Email
     */
    public void sendHtmlEmail(String to, String subject, String content) {
        boolean success = false;
        String failReason = null;
        try {
            if (mailSender == null) {
                log.warn("JavaMailSender is not configured. Email to {} skipped.", to);
                failReason = "JavaMailSender not configured";
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            if (from != null && !from.isEmpty()) {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("HTML Email sent to {}", to);
            success = true;
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}", to, e);
            failReason = e.getMessage();
            throw new RuntimeException("HTML Email sending failed", e);
        } finally {
            if (emailRecordService != null) {
                emailRecordService.saveRecord(to, subject, content, success, failReason);
            }
        }
    }
}
