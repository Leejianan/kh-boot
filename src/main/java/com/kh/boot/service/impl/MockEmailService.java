package com.kh.boot.service.impl;

import com.kh.boot.service.EmailService;
import com.kh.boot.util.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of Email Service.
 * In a real application, you would integrate with an SMTP server or an Email
 * API provider.
 */
@Slf4j
@Service
@Primary
public class MockEmailService implements EmailService {

    @Autowired(required = false)
    private EmailUtils emailUtils;

    // 模拟存储 (生产环境应当使用 Redis)
    private final Map<String, String> codeStore = new ConcurrentHashMap<>();

    @Override
    public String sendCode(String email) {
        // 生成 4 位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        codeStore.put(email, code);

        log.info("=========== MOCK 邮件发送器 ===========");
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
        String storedCode = codeStore.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            codeStore.remove(email);
            return true;
        }
        return false;
    }
}
