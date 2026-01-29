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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
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
    private final TaskScheduler taskScheduler;

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

        // 只有房主可以同步状态给其他人
        ScreeningRoomDTO roomDetail = roomService.getRoomDetail(roomId);
        String currentUsername = principal.getName();

        if (roomDetail == null || !currentUsername.equals(roomDetail.getOwnerUsername())) {
            log.debug("Rejecting sync from non-owner: user={}, owner={}", currentUsername,
                    roomDetail != null ? roomDetail.getOwnerUsername() : "null");
            return;
        }

        log.info("Room sync from owner {}: room={}, isPlaying={}, currentTime={}",
                currentUsername, roomId, isPlaying, currentTime);

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

        String userId = null;
        String username = principal.getName();

        // 尝试获取完整的用户信息
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
            if (auth.getPrincipal() instanceof com.kh.boot.security.domain.LoginUser) {
                com.kh.boot.security.domain.LoginUser loginUser = (com.kh.boot.security.domain.LoginUser) auth
                        .getPrincipal();
                userId = loginUser.getUserId();
                username = loginUser.getUsername();
            }
        }

        if (userId == null) {
            log.warn("Could not extract userId from principal for danmaku");
            // 尝试通过 username 从数据库查找? 或者直接因为 DB 约束而失败。
            // 这里我们无法修复缺少 userId 的情况，只能让其报错或提前返回。
            // 为了避免 DataIntegrityViolationException，我们应该检查 userId
            log.error("Sending danmaku failed: userId is null");
            return;
        }

        String roomId = (String) payload.get("roomId");
        String videoId = (String) payload.get("videoId");
        String content = (String) payload.get("content");
        Number videoTimeNum = (Number) payload.get("videoTime");
        int videoTime = videoTimeNum != null ? videoTimeNum.intValue() : 0;
        String color = (String) payload.get("color");
        String position = (String) payload.get("position");

        log.info("Danmaku from {}({}): room={}, content={}", username, userId, roomId, content);

        // 保存弹幕并广播（sendDanmaku 内部会广播）
        danmakuService.sendDanmaku(userId, username, roomId, videoId, content, videoTime, color, position);
    }

    /**
     * 加入房间通知
     * 客户端发送到: /app/room.join
     */
    @MessageMapping("/room.join")
    public void joinRoom(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        log.info("joinRoom called: sessionId={}, principal={}, payload={}",
                headerAccessor.getSessionId(),
                principal != null ? principal.getName() : "NULL",
                payload);

        if (principal == null) {
            log.warn("joinRoom: No principal found in header accessor, sessionId={}", headerAccessor.getSessionId());
            return;
        }

        String roomId = (String) payload.get("roomId");
        String username = principal.getName();
        log.info("=== User {} joining room {} via WebSocket ===", username, roomId);

        // 确保将用户添加到房间成员列表（如果不在）
        try {
            roomService.joinRoom(roomId);
        } catch (Exception e) {
            log.warn("Failed to join room in DB: {}", e.getMessage());
            // 继续执行，因为可能是重复加入或者其他非致命错误，不应该阻断 WebSocket 流程
        }

        // 获取房间最新状态并发送给新加入的用户
        ScreeningRoomDTO roomDetail = roomService.getRoomDetail(roomId);
        log.info("Room detail fetched: videoId={}, isPlaying={}, currentTime={}",
                roomDetail.getCurrentVideoId(), roomDetail.getIsPlaying(), roomDetail.getCurrentTime());

        // 发送给刚加入的用户当前播放状态（包含完整的同步信息）
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/room/state",
                Map.of(
                        "type", "room_state",
                        "room", roomDetail,
                        "timestamp", System.currentTimeMillis()));
        log.info("Sent room state to user {}", username);

        // 延迟500ms发送同步消息，确保前端已经订阅并加载了视频
        taskScheduler.schedule(() -> {
            try {
                log.info("=== Sending delayed sync message to user {} ===", username);
                // 同时发送一个明确的同步消息，确保新用户能够同步播放进度
                Map<String, Object> syncMessage = Map.of(
                        "type", "sync",
                        "isPlaying", roomDetail.getIsPlaying() != null && roomDetail.getIsPlaying() == 1,
                        "currentTime", roomDetail.getCurrentTime() != null ? roomDetail.getCurrentTime() : 0,
                        "videoId", roomDetail.getCurrentVideoId() != null ? roomDetail.getCurrentVideoId() : "",
                        "videoUrl", roomDetail.getCurrentVideoUrl() != null ? roomDetail.getCurrentVideoUrl() : "",
                        "sender", "system",
                        "timestamp", System.currentTimeMillis());

                log.info("Sync message content: {}", syncMessage);
                messagingTemplate.convertAndSendToUser(
                        username,
                        "/queue/room/sync",
                        syncMessage);

                log.info("Sent delayed sync state to new user {}: isPlaying={}, currentTime={}",
                        username,
                        roomDetail.getIsPlaying(),
                        roomDetail.getCurrentTime());
            } catch (Exception e) {
                log.error("Failed to send delayed sync message to user {}", username, e);
            }
        }, Instant.now().plusMillis(500));

        // 广播给房间内其他成员 - 已在 roomService.joinRoom 中处理
        // roomService.joinRoom 内部已调用 notifyRoomMembers 广播 member_join
        // 所以这里不需要再发一次，避免重复通知

        log.info("Broadcasted member_join event to room {}", roomId);
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
        String username = principal.getName();
        log.info("User {} left room {} via WebSocket", username, roomId);

        // 检查是否是房主离开
        try {
            ScreeningRoomDTO roomDetail = roomService.getRoomDetail(roomId);
            boolean isOwner = username.equals(roomDetail.getOwnerUsername());

            // 广播给房间内其他成员
            messagingTemplate.convertAndSend("/topic/room/" + roomId,
                    Map.of(
                            "type", "member_leave",
                            "username", username,
                            "isOwner", isOwner,
                            "message", isOwner ? "房主已离开，播放已暂停" : username + " 离开了放映室",
                            "timestamp", System.currentTimeMillis()));

            if (isOwner) {
                log.info("Owner {} left room {}, notifying other members", username, roomId);
            }
        } catch (Exception e) {
            log.error("Error handling room leave for user {}: {}", username, e.getMessage());
        }
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
