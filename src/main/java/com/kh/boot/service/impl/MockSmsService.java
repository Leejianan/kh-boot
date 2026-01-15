package com.kh.boot.service.impl;

import com.kh.boot.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class MockSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(MockSmsService.class);

    private static final java.util.Map<String, CodeEntry> smsCache = new java.util.concurrent.ConcurrentHashMap<>();

    private static class CodeEntry {
        String code;
        long expiry;

        public CodeEntry(String code, long expiry) {
            this.code = code;
            this.expiry = expiry;
        }
    }

    @Override
    public boolean sendCode(String phone) {
        // Generate 6 digit code
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        
        // Store in cache (5 minutes)
        smsCache.put(phone, new CodeEntry(code, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
        
        logger.info("==========================================");
        logger.info(" [MOCK SMS] Sending code to {}: {}", phone, code);
        logger.info("==========================================");
        
        return true;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        CodeEntry entry = smsCache.get(phone);
        if (entry != null) {
            if (System.currentTimeMillis() > entry.expiry) {
                smsCache.remove(phone);
                return false;
            }
            if (entry.code.equals(code)) {
                smsCache.remove(phone); // Burn after reading
                return true;
            }
        }
        return false;
    }
}
