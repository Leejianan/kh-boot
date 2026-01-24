package com.kh.boot.util;

import com.kh.boot.service.ConfigService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import com.kh.boot.service.EmailRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
public class EmailUtils {

    @Autowired
    private ConfigService configService;

    @Autowired
    private EmailRecordService emailRecordService;

    /**
     * Dynamically build JavaMailSender from DB config
     */
    //dfdfd
    private JavaMailSender getJavaMailSender() {
        String host = configService.getValueByKey("sys.mail.host");
        String port = configService.getValueByKey("sys.mail.port");
        String username = configService.getValueByKey("sys.mail.username");
        String password = configService.getValueByKey("sys.mail.password");
        String protocol = configService.getValueByKey("sys.mail.protocol");

        if (host == null || port == null || username == null || password == null) {
            log.warn("Mail config missing in DB. details: host={}, port={}, user=***", host, port);
            return null;
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        try {
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(port));
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            mailSender.setProtocol(protocol != null ? protocol : "smtp");
            mailSender.setDefaultEncoding("UTF-8");

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } catch (Exception e) {
            log.error("Failed to create JavaMailSender: {}", e.getMessage());
            return null;
        }

        return mailSender;
    }

    /**
     * Send Simple Email
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        boolean success = false;
        String failReason = null;
        JavaMailSender mailSender = getJavaMailSender();

        try {
            if (mailSender == null) {
                log.warn("JavaMailSender is not configured. Email to {} skipped.", to);
                failReason = "JavaMailSender not configured";
                return;
            }

            String from = configService.getValueByKey("sys.mail.username");
            String fromName = configService.getValueByKey("sys.mail.fromName");

            SimpleMailMessage message = new SimpleMailMessage();
            if (from != null && !from.isEmpty()) {
                if (fromName != null && !fromName.isEmpty()) {
                    message.setFrom(String.format("%s <%s>", fromName, from));
                } else {
                    message.setFrom(from);
                }
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
        JavaMailSender mailSender = getJavaMailSender();

        try {
            if (mailSender == null) {
                log.warn("JavaMailSender is not configured. Email to {} skipped.", to);
                failReason = "JavaMailSender not configured";
                return;
            }

            String from = configService.getValueByKey("sys.mail.username");
            String fromName = configService.getValueByKey("sys.mail.fromName");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (from != null && !from.isEmpty()) {
                if (fromName != null && !fromName.isEmpty()) {
                    helper.setFrom(from, fromName);
                } else {
                    helper.setFrom(from);
                }
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
