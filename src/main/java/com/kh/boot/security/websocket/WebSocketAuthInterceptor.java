package com.kh.boot.security.websocket;

import com.kh.boot.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;

/**
 * WebSocket 认证拦截器
 * 验证 STOMP 连接中的 JWT Token
 *
 * @author harlan
 * @since 2024-01-20
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService defaultUserDetailsService;
    private final ApplicationContext applicationContext;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil,
            @Qualifier("userDetailsServiceImpl") UserDetailsService defaultUserDetailsService,
            ApplicationContext applicationContext) {
        this.jwtUtil = jwtUtil;
        this.defaultUserDetailsService = defaultUserDetailsService;
        this.applicationContext = applicationContext;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                log.debug("WebSocket CONNECT command received, sessionId={}", accessor.getSessionId());
                // 从 STOMP 头中获取 token
                String token = accessor.getFirstNativeHeader("Authorization");
                log.debug("Authorization header: {}", token != null ? "Present" : "Missing");

                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);

                    try {
                        String username = jwtUtil.extractUsername(token);
                        log.debug("Extracted username from token: {}", username);

                        if (username != null && !jwtUtil.isTokenExpired(token)) {
                            String userType = jwtUtil.extractUserType(token);
                            UserDetailsService userDetailsService = defaultUserDetailsService;

                            if ("member".equals(userType) && applicationContext.containsBean("memberUserDetailsService")) {
                                userDetailsService = applicationContext.getBean("memberUserDetailsService",
                                        UserDetailsService.class);
                            } else if ("admin".equals(userType)) {
                                userDetailsService = defaultUserDetailsService;
                            }

                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                            // 验证 token 是否有效
                            if (jwtUtil.validateToken(token, username)) {
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());

                                // 设置用户信息到 STOMP session
                                accessor.setUser(auth);
                                SecurityContextHolder.getContext().setAuthentication(auth);

                                log.info("WebSocket user authenticated: {}, sessionId={}", username, accessor.getSessionId());
                            } else {
                                log.warn("Token validation failed for user: {}", username);
                            }
                        } else {
                            log.warn("Token expired or username is null");
                        }
                    } catch (Exception e) {
                        log.error("WebSocket authentication failed for token: {}", e.getMessage());
                    }
                } else {
                    log.warn("No valid Authorization header found in CONNECT");
                }
            } else if (accessor != null && accessor.getCommand() != null) {
                // 记录其他命令的 principal 状态（排除 DISCONNECT）
                if (accessor.getCommand() != StompCommand.DISCONNECT) {
                    log.debug("WebSocket command: {}, sessionId={}, principal={}", 
                            accessor.getCommand(), 
                            accessor.getSessionId(),
                            accessor.getUser() != null ? accessor.getUser().getName() : "NULL");
                }
            }
        } catch (Exception e) {
            log.error("Error in WebSocket interceptor: {}", e.getMessage(), e);
        }

        return message;
    }
}
