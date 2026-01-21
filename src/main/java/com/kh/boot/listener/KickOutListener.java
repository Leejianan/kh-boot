package com.kh.boot.listener;

import com.kh.boot.event.KickOutEvent;
import com.kh.boot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KickOutListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleKickOutEvent(KickOutEvent event) {
        log.info("Handling KickOutEvent for user: {}", event.getUsername());
        try {
            notificationService.sendKickOut(event.getUsername(), event.getUserType(), event.getToken(),
                    event.getReason());
        } catch (Exception e) {
            log.error("Failed to send kick-out notification", e);
        }
    }
}
