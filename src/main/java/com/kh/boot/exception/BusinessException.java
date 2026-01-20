package com.kh.boot.exception;

/**
 * 业务异常
 *
 * @author harlan
 * @since 2024-01-20
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code = 400;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
