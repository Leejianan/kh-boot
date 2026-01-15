package com.kh.boot.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manual Validation Utility
 * Wraps jakarta.validation APIs for programmatic validation.
 */
public class ValidationUtils {

    // Thread-safe validator instance
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * Validates a bean.
     * Throws IllegalArgumentException if validation fails.
     *
     * @param bean   The object to validate
     * @param groups Optional validation groups
     * @param <T>    The type of the object
     */
    public static <T> void validate(T bean, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean, groups);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates a bean and returns validation result.
     * Does not throw exception.
     *
     * @param bean   The object to validate
     * @param groups Optional validation groups
     * @param <T>    The type of the object
     * @return Set of violations (empty if valid)
     */
    public static <T> Set<ConstraintViolation<T>> validateResult(T bean, Class<?>... groups) {
        return validator.validate(bean, groups);
    }
}
