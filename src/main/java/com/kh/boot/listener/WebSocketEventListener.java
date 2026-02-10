package com.kh.boot.listener;

import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.service.ScreeningRoomService;
import com.kh.boot.cache.AuthCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 事件监听器
 * 处理 WebSocket 连接断开等事件，支持自动下线逻辑
 *
 * @author harlan
 * @since 2026-01-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ScreeningRoomService screeningRoomService;
    private final AuthCache authCache;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 监听 Session 连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
            if (auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                authCache.incrementConnection(loginUser.getUsername(), loginUser.getUserType());
                log.debug("WebSocket connected: {}, type: {}, current connections: {}", 
                        loginUser.getUsername(), loginUser.getUserType(), 
                        authCache.getConnectionCount(loginUser.getUsername(), loginUser.getUserType()));
            }
        }
    }

    /**
     * 监听 Session 断开事件
     * 当用户关闭浏览器、刷新页面或网络中断导致 WebSocket 断开时触发
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal principal = event.getUser();

        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
            if (auth.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) auth.getPrincipal();
                String username = loginUser.getUsername();
                String userType = loginUser.getUserType();
                String userId = loginUser.getUserId();

                log.info("WebSocket Disconnecting: {} ({})", username, userId);

                // 1. 处理业务逻辑 (放映厅)
                try {
                    screeningRoomService.handleUserOffline(userId);
                } catch (Exception e) {
                    log.error("Error handling user offline: {}", e.getMessage(), e);
                }

                // 2. 递减计数
                authCache.decrementConnection(username, userType);
                long currentCount = authCache.getConnectionCount(username, userType);
                log.debug("Remaining connections for {}: {}", username, currentCount);

                // 3. 如果计数归零，延迟检查并执行登出
                if (currentCount <= 0) {
                    // 延迟 10 秒执行，给予刷新页面或重新连接的时间
                    scheduler.schedule(() -> {
                        try {
                            long latestCount = authCache.getConnectionCount(username, userType);
                            if (latestCount <= 0) {
                                log.info("[Auto-Logout] No active connections for {} after delay. Invalidating session.", username);
                                authCache.remove(username, userType);
                            } else {
                                log.debug("[Auto-Logout] User {} re-connected during delay. Aborting logout.", username);
                            }
                        } catch (Exception e) {
                            log.error("Error in auto-logout task", e);
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        }
    }
}
