package com.kh.boot.service.impl;

import com.kh.boot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of NotificationService using Spring Messaging (STOMP).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendKickOut(String username, String userType, String token, String reason) {
        log.info(">>> EXECUTE sendKickOut for user: [{}], token: [{}], reason: [{}]", username, token, reason);
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "KICK_OUT");
        payload.put("reason", reason);
        if (token != null) {
            payload.put("token", token);
        }

        // Point-to-point message via user destination
        // Destination will be /user/{username}/queue/kick
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/kick", payload);
            log.info(">>> SUCCESS sendKickOut dispatched to /user/{}/queue/kick", username);
        } catch (Exception e) {
            log.error(">>> ERROR sending kick-out message", e);
        }
    }

    @Override
    public void broadcastAnnouncement(String content) {
        log.info("Broadcasting system announcement: {}", content);
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "ANNOUNCEMENT");
        payload.put("content", content);

        // Broadcast to all subscribers of /topic/announcement
        messagingTemplate.convertAndSend("/topic/announcement", payload);
    }
}
