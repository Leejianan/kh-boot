package com.kh.boot.controller;

import com.kh.boot.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public Result<Object> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = (String) request.getAttribute("jakarta.servlet.error.message");

        if (statusCode == null) {
            statusCode = 500;
        }

        if (message == null || message.isEmpty()) {
            message = "Internal Server Error";
            if (statusCode == 404) {
                message = "End point not found";
            }
        }

        return Result.error(statusCode, message);
    }
}
