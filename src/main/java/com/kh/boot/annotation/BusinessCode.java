package com.kh.boot.annotation;

import java.lang.annotation.*;

/**
 * Annotation for automatic business code generation.
 * Can be used on fields of Entity classes extending KhBaseEntity.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessCode {

    /**
     * Business key used for rule lookup and sequence persistence.
     */
    String businessKey() default "";

    /**
     * Prefix for the generated code.
     */
    String prefix() default "";

    /**
     * Date format part. If empty, no date part will be included.
     * Example: "yyyyMMdd"
     */
    String dateFormat() default "";

    /**
     * Width of the numeric sequence part.
     * Example: 4 -> "0001"
     */
    int width() default 5;
}
