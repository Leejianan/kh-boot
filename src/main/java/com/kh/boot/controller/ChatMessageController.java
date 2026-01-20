package com.kh.boot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.entity.KhChatMessage;
import com.kh.boot.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息 REST 控制器
 *
 * @author harlan
 * @since 2024-01-20
 */
@Tag(name = "聊天消息", description = "获取聊天记录、已读状态等")
@RestController
@RequestMapping("/api/chat/message")
@RequiredArgsConstructor
public class ChatMessageController extends BaseController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "获取聊天记录", description = "获取与指定好友的聊天记录")
    @GetMapping("/history/{friendId}")
    public Result<PageData<KhChatMessage>> getHistory(
            @PathVariable String friendId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "50") int size) {
        IPage<KhChatMessage> page = chatMessageService.getHistory(friendId, current, size);
        return pageSuccess(page);
    }

    @Operation(summary = "标记消息已读", description = "标记指定消息为已读")
    @PostMapping("/read")
    public Result<Void> markAsRead(@RequestBody List<String> messageIds) {
        chatMessageService.markAsRead(messageIds);
        return success();
    }

    @Operation(summary = "标记与好友的所有消息已读")
    @PostMapping("/read-all/{friendId}")
    public Result<Void> markAllAsRead(@PathVariable String friendId) {
        chatMessageService.markAllAsRead(friendId);
        return success();
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread")
    public Result<Map<String, Integer>> getUnreadCount() {
        int count = chatMessageService.getUnreadCount();
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return success(result);
    }

    @Operation(summary = "获取与指定好友的未读消息数")
    @GetMapping("/unread/{friendId}")
    public Result<Map<String, Integer>> getUnreadCountByFriend(@PathVariable String friendId) {
        int count = chatMessageService.getUnreadCountByFriend(friendId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return success(result);
    }
}
