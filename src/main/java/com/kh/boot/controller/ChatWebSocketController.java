package com.kh.boot.controller;

import com.kh.boot.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 聊天控制器
 * 处理通过 STOMP 发送的消息
 *
 * @author harlan
 * @since 2024-01-20
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;

    /**
     * 发送聊天消息
     * 客户端发送到: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("WebSocket message received without authentication");
            return;
        }

        String senderUsername = principal.getName();
        String receiverId = (String) payload.get("receiverId");
        String content = (String) payload.get("content");
        Integer msgType = payload.get("msgType") != null ? ((Number) payload.get("msgType")).intValue() : 0;

        log.info("Chat message from {} to {}: {}", senderUsername, receiverId, content);

        // 保存并推送消息
        chatMessageService.sendMessageByUsername(senderUsername, receiverId, content, msgType);
    }

    /**
     * 标记消息为已读
     * 客户端发送到: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            return;
        }

        String friendId = (String) payload.get("friendId");

        if (friendId != null) {
            chatMessageService.markAllAsReadByUsername(principal.getName(), friendId);
        }
    }
}
