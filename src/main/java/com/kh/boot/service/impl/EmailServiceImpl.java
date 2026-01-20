package com.kh.boot.service.impl;

import com.kh.boot.service.EmailService;
import com.kh.boot.util.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of Email Service.
 * In a real application, you would integrate with an SMTP server or an Email
 * API provider.
 */
@Slf4j
@Service
@Primary
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private EmailUtils emailUtils;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:email:";
    private static final long CAPTCHA_EXPIRATION = 5; // minutes

    @Override
    public String sendCode(String email) {
        // 生成 4 位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));

        // Store in Redis with expiration
        String key = CAPTCHA_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, java.time.Duration.ofMinutes(CAPTCHA_EXPIRATION));

        log.info("=========== 邮件发送服务 ===========");
        log.info("收件人: {}", email);
        log.info("内容: 您的验证码是 {}", code);
        log.info("========================================");

        // 如果配置了 EmailUtils (Starter Mail 存在)，尝试发送真实邮件
        if (emailUtils != null) {
            try {
                emailUtils.sendSimpleEmail(email, "登录验证码", "您的验证码是: " + code);
                log.info("通过 EmailUtils 发送真实邮件成功");
            } catch (Exception e) {
                log.warn("发送真实邮件失败: {}", e.getMessage());
            }
        }

        return code;
    }

    @Override
    public boolean verifyCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        String key = CAPTCHA_KEY_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    @Override
    public void sendEmail(String to, String subject, String content) {
        if (emailUtils != null) {
            emailUtils.sendHtmlEmail(to, subject, content);
        } else {
            log.warn("EmailUtils not configured, skipping email to {}", to);
        }
    }
}
