package com.kh.boot.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Main Auto-Configuration for kh-boot module.
 * Enable this module by adding it to scan path or using @EnableKhBoot (if
 * defined).
 */
@AutoConfiguration
@Import(KHBootConfig.class)
public class KhBootAutoConfiguration {
}
