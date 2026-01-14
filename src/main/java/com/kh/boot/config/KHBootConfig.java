package com.kh.boot.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Main configuration for KH-Boot framework.
 * This class ensures that default properties from the framework are loaded
 * and all framework components are scanned and registered.
 */
@Configuration
@PropertySource(value = "classpath:kh-boot-default.properties", encoding = "UTF-8")
@ComponentScan(basePackages = "com.kh.boot")
@org.mybatis.spring.annotation.MapperScan("com.kh.boot.mapper")
public class KHBootConfig {
}
