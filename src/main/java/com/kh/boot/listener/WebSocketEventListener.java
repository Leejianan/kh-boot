package com.kh.boot.listener;

import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.service.ScreeningRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * WebSocket 事件监听器
 * 处理 WebSocket 连接断开等事件
 *
 * @author harlan
 * @since 2026-01-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ScreeningRoomService screeningRoomService;

    /**
     * 监听 Session 断开事件
     * 当用户关闭浏览器、刷新页面或网络中断导致 WebSocket 断开时触发
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal principal = event.getUser();

        if (principal != null) {
            String userId = null;
            String username = principal.getName();

            // 尝试提取 UserId
            if (principal instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
                if (auth.getPrincipal() instanceof LoginUser) {
                    LoginUser loginUser = (LoginUser) auth.getPrincipal();
                    userId = loginUser.getUserId();
                }
            }

            if (userId != null) {
                log.info("User Disconnected: {} ({})", username, userId);
                // 调用服务处理用户离线逻辑
                try {
                    screeningRoomService.handleUserOffline(userId);
                } catch (Exception e) {
                    log.error("Error handling user offline: {}", e.getMessage(), e);
                }
            } else {
                log.warn("User Disconnected but userId not found for principal: {}", username);
            }
        }
    }
}
