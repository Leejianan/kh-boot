package com.kh.boot.util;

import com.kh.boot.annotation.BusinessCode;
import com.kh.boot.entity.base.KhBaseEntity;
import com.kh.boot.entity.KhSerialNumber;
import com.kh.boot.service.SerialNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * Populates business code for entities using reflection and
 * SerialNumberService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SerialNumberGenerator {

    private final SerialNumberService serialNumberService;

    @PostConstruct
    public void init() {
        EntityUtils.setSerialNumberGenerator(this);
    }

    /**
     * Pre-scans an entity class to register or warm up its business code rules.
     */
    public void preScan(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(BusinessCode.class)) {
                BusinessCode annotation = field.getAnnotation(BusinessCode.class);
                String businessKey = annotation.businessKey().isEmpty() ? clazz.getSimpleName().toLowerCase()
                        : annotation.businessKey();

                KhSerialNumber sn = new KhSerialNumber();
                sn.setBusinessKey(businessKey);
                sn.setRulePrefix(annotation.prefix());
                sn.setRuleDateFormat(annotation.dateFormat());
                sn.setRuleWidth(annotation.width());

                try {
                    serialNumberService.saveRule(sn);
                } catch (Exception e) {
                    if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry")
                            || e.getMessage().contains("ConstraintViolationException"))) {
                        log.warn("Rule for [{}] already exists (duplicate entry ignored).", businessKey);
                    } else {
                        log.error("Failed to pre-cache rule for [{}]: {}", businessKey, e.getMessage());
                    }
                }
            }
        }
    }

    public void populateBusinessCode(KhBaseEntity entity) {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(BusinessCode.class)) {
                BusinessCode annotation = field.getAnnotation(BusinessCode.class);
                String businessKey = annotation.businessKey().isEmpty() ? clazz.getSimpleName().toLowerCase()
                        : annotation.businessKey();

                try {
                    String code = serialNumberService.generateNext(
                            businessKey,
                            annotation.prefix(),
                            annotation.dateFormat(),
                            annotation.width());

                    field.setAccessible(true);
                    field.set(entity, code);
                } catch (IllegalAccessException e) {
                    log.error("Failed to populate business code for [{}]: {}", clazz.getSimpleName(), e.getMessage());
                }
            }
        }
    }
}
