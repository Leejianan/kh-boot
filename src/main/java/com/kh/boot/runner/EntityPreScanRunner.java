package com.kh.boot.runner;

import com.kh.boot.util.SerialNumberGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.IOException;

/**
 * Runner to pre-scan entities and warm up the serial number generator cache.
 */
@Slf4j
@Component
public class EntityPreScanRunner implements ApplicationRunner {

    @Autowired
    private SerialNumberGenerator serialNumberGenerator;

    /**
     * Packages to scan for entities, comma separated.
     * Default to kh-boot's own entity package.
     */
    @org.springframework.beans.factory.annotation.Value("${kh.boot.serial-number.scan-packages:com.kh.boot.entity}")
    private String scanPackages;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting entity pre-scan for BusinessCode cache with patterns: {}", scanPackages);

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);

        String[] packages = scanPackages.split(",");
        for (String pkg : packages) {
            pkg = pkg.trim();
            if (pkg.isEmpty())
                continue;

            // Convert package/pattern to Ant search pattern
            // e.g. com.kh.entity -> classpath*:com/kh/entity/**/*.class
            // e.g. **.entity -> classpath*:**/entity/**/*.class
            String pattern = "classpath*:" + pkg.replace(".", "/") + "/**/*.class";

            try {
                Resource[] resources = resolver.getResources(pattern);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        try {
                            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                            String className = metadataReader.getClassMetadata().getClassName();

                            Class<?> clazz = ClassUtils.forName(className, getClass().getClassLoader());
                            serialNumberGenerator.preScan(clazz);
                            log.debug("Pre-scanned entity class: {}", className);
                        } catch (Throwable e) {
                            // Skip classes that cannot be loaded, e.g. due to missing dependencies
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Failed to resolve scan pattern: {}", pattern, e);
            }
        }

        log.info("Entity pre-scan completed for all configured patterns.");
    }
}
