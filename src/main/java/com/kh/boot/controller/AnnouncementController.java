package com.kh.boot.controller;

import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Announcement Controller
 * Allows administrators to send system-wide notifications.
 */
@Tag(name = "Announcement Management", description = "Send system-wide broadcasts")
@RestController
@RequestMapping("/admin/system/announcement")
@RequiredArgsConstructor
public class AnnouncementController extends BaseController {

    private final NotificationService notificationService;

    @Operation(summary = "Send Global Announcement", description = "Broadcast a message to all online users")
    @PostMapping
    @PreAuthorize("hasAuthority('system:root:announce:send')")
    public Result<Void> sendAnnouncement(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return error("公告内容不能为空");
        }
        notificationService.broadcastAnnouncement(content);
        return success();
    }
}
