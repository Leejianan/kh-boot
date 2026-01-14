package com.kh.boot.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Custom startup runner for KH-Boot.
 * Displays useful information after the application has started.
 */
@Slf4j
@Component
public class CustomStartupRunner implements ApplicationRunner {

    @Autowired
    private Environment environment;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================================================");
        log.info("  KH-Boot (RBAC Framework) started successfully! ");

        String port = environment.getProperty("server.port");
        if (port == null) {
            port = "8080";
        }

        String contextPath = environment.getProperty("server.servlet.context-path");
        if (contextPath == null || "/".equals(contextPath)) {
            contextPath = "";
        }

        log.info("  Standard API Docs:  http://localhost:{}{}/doc.html", port, contextPath);
        log.info("  Online User Monitor: Active (Caffeine Cache enabled)");
        log.info("========================================================================");
    }
}
