package com.kh.boot.exception;

import com.kh.boot.common.Result;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private <T> ResponseEntity<Result<T>> buildResponse(int code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.error(code, message));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException e) {
        log.error("Access Denied: {}", e.getMessage());
        return buildResponse(403, "Access Denied: " + e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("Not Found: {}", e.getRequestURL());
        return buildResponse(404, "Endpoint not found: " + e.getRequestURL(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Error: {}", e.getMessage());
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return buildResponse(400, "Validation Error: " + msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business Error: {}", e.getMessage());
        return buildResponse(e.getCode(), e.getMessage(), HttpStatus.OK);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Argument Error: {}", e.getMessage());
        return buildResponse(400, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("System Error", e);
        return buildResponse(500, "Server Error: " + (e.getMessage() != null ? e.getMessage() : "Internal Error"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        // 这是一个常见的客户端断开连接异常，不需要打印堆栈，甚至可以忽略
        log.warn("客户端中断了连接: {}", e.getMessage());
    }
}
