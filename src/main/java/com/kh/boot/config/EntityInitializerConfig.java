package com.kh.boot.config;

import com.kh.boot.util.EntityUtils;
import com.kh.boot.util.SerialNumberGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EntityInitializerConfig {

    private final SerialNumberGenerator serialNumberGenerator;

    @PostConstruct
    public void init() {
        EntityUtils.setSerialNumberGenerator(serialNumberGenerator);
    }
}
