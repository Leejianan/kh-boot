package com.kh.boot.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());

        // Log basic request info as it arrives
        String queryString = request.getQueryString();
        String params = (queryString != null) ? "?" + queryString : "";
        logger.debug(">>> Request Start: [{}] {}{}", request.getMethod(), request.getRequestURI(), params);

        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler, @Nullable Exception ex)
            throws Exception {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime == null)
            return;

        long executeTime = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        // Use different log templates for success vs error
        if (ex != null) {
            logger.error("<<< Request Failed: [{}] {} | Status: {} | Time: {}ms | Error: {}",
                    request.getMethod(), request.getRequestURI(), status, executeTime, ex.getMessage());
        } else {
            logger.info("<<< Request End: [{}] {} | Status: {} | Time: {}ms | IP: {}",
                    request.getMethod(), request.getRequestURI(), status, executeTime, request.getRemoteAddr());
        }
    }
}
