package com.kh.boot.controller;

import com.kh.boot.dto.ScreeningRoomDTO;
import com.kh.boot.service.DanmakuService;
import com.kh.boot.service.ScreeningRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * 放映室 WebSocket 控制器
 * 处理实时同步：播放状态、弹幕推送
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final ScreeningRoomService roomService;
    private final DanmakuService danmakuService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 同步播放状态
     * 客户端发送到: /app/room.sync
     * 广播到: /topic/room/{roomId}/sync
     */
    @MessageMapping("/room.sync")
    public void syncPlayStatus(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("Room sync received without authentication");
            return;
        }

        String roomId = (String) payload.get("roomId");
        Boolean isPlaying = (Boolean) payload.get("isPlaying");
        Number currentTimeNum = (Number) payload.get("currentTime");
        int currentTime = currentTimeNum != null ? currentTimeNum.intValue() : 0;

        log.info("Room sync from {}: room={}, isPlaying={}, currentTime={}",
                principal.getName(), roomId, isPlaying, currentTime);

        // 更新数据库状态
        roomService.syncPlayStatus(roomId, isPlaying != null && isPlaying, currentTime);

        // 广播给房间内所有成员
        Map<String, Object> syncData = Map.of(
                "type", "sync",
                "isPlaying", isPlaying != null && isPlaying,
                "currentTime", currentTime,
                "sender", principal.getName(),
                "timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/sync", syncData);
    }

    /**
     * 发送弹幕
     * 客户端发送到: /app/room.danmaku
     * 广播到: /topic/room/{roomId}/danmaku
     */
    @MessageMapping("/room.danmaku")
    public void sendDanmaku(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("Danmaku received without authentication");
            return;
        }

        String roomId = (String) payload.get("roomId");
        String videoId = (String) payload.get("videoId");
        String content = (String) payload.get("content");
        Number videoTimeNum = (Number) payload.get("videoTime");
        int videoTime = videoTimeNum != null ? videoTimeNum.intValue() : 0;
        String color = (String) payload.get("color");
        String position = (String) payload.get("position");

        log.info("Danmaku from {}: room={}, content={}", principal.getName(), roomId, content);

        // 保存弹幕并广播（sendDanmaku 内部会广播）
        danmakuService.sendDanmaku(roomId, videoId, content, videoTime, color, position);
    }

    /**
     * 加入房间通知
     * 客户端发送到: /app/room.join
     */
    @MessageMapping("/room.join")
    public void joinRoom(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            return;
        }

        String roomId = (String) payload.get("roomId");
        log.info("User {} joined room {} via WebSocket", principal.getName(), roomId);

        // 获取房间最新状态并发送给新加入的用户
        ScreeningRoomDTO roomDetail = roomService.getRoomDetail(roomId);

        // 发送给刚加入的用户当前播放状态
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/room/state",
                Map.of(
                        "type", "room_state",
                        "room", roomDetail,
                        "timestamp", System.currentTimeMillis()));

        // 广播给房间内其他成员
        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                Map.of(
                        "type", "member_join",
                        "username", principal.getName(),
                        "memberCount", roomDetail.getMemberCount(),
                        "timestamp", System.currentTimeMillis()));
    }

    /**
     * 离开房间通知
     * 客户端发送到: /app/room.leave
     */
    @MessageMapping("/room.leave")
    public void leaveRoom(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            return;
        }

        String roomId = (String) payload.get("roomId");
        log.info("User {} left room {} via WebSocket", principal.getName(), roomId);

        // 广播给房间内其他成员
        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                Map.of(
                        "type", "member_leave",
                        "username", principal.getName(),
                        "timestamp", System.currentTimeMillis()));
    }

    /**
     * 切换视频通知
     * 客户端发送到: /app/room.switchVideo
     */
    @MessageMapping("/room.switchVideo")
    public void switchVideo(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            return;
        }

        String roomId = (String) payload.get("roomId");
        String videoId = (String) payload.get("videoId");

        log.info("User {} switching video in room {}: videoId={}", principal.getName(), roomId, videoId);

        // 调用服务切换视频（内部会验证房主权限）
        try {
            roomService.switchVideoInternal(roomId, videoId);

            // 获取更新后的房间状态
            ScreeningRoomDTO roomDetail = roomService.getRoomDetail(roomId);

            // 广播给房间内所有成员
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/video",
                    Map.of(
                            "type", "video_switch",
                            "videoId", videoId,
                            "videoTitle",
                            roomDetail.getCurrentVideoTitle() != null ? roomDetail.getCurrentVideoTitle() : "",
                            "videoUrl", roomDetail.getCurrentVideoUrl() != null ? roomDetail.getCurrentVideoUrl() : "",
                            "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Failed to switch video: {}", e.getMessage());
        }
    }
}
