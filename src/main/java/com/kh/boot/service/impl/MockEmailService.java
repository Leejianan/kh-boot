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

    // Simulate storage (Use Redis in production)
    private final Map<String, String> codeStore = new ConcurrentHashMap<>();

    @Override
    public String sendCode(String email) {
        // Generate a 4-digit code
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        codeStore.put(email, code);

        log.info("=========== MOCK EMAIL SENDER ===========");
        log.info("To: {}", email);
        log.info("Content: Your verification code is {}", code);
        log.info("========================================");

        // If EmailUtils is available (Starter Mail is present), try sending real email
        if (emailUtils != null) {
            try {
                emailUtils.sendSimpleEmail(email, "Login Verification Code", "Your code is: " + code);
                log.info("Real email sent via EmailUtils");
            } catch (Exception e) {
                log.warn("Failed to send real email: {}", e.getMessage());
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
