package com.kh.boot.controller;

import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.dto.FriendDTO;
import com.kh.boot.dto.KhUserDTO;
import com.kh.boot.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友管理控制器
 *
 * @author harlan
 * @since 2024-01-20
 */
@Tag(name = "好友管理", description = "好友添加、查询、删除等操作")
@RestController
@RequestMapping("/api/chat/friend")
@RequiredArgsConstructor
public class FriendController extends BaseController {

    private final FriendService friendService;

    @Operation(summary = "搜索用户", description = "通过手机号或邮箱搜索用户")
    @GetMapping("/search")
    public Result<List<KhUserDTO>> searchUser(@RequestParam String keyword) {
        return success(friendService.searchUser(keyword));
    }

    @Operation(summary = "发送好友申请", description = "向指定用户发送好友申请")
    @PostMapping("/add")
    public Result<Void> addFriend(@RequestParam String friendId) {
        friendService.addFriend(friendId);
        return success();
    }

    @Operation(summary = "处理好友申请", description = "接受或拒绝好友申请")
    @PutMapping("/handle")
    public Result<Void> handleRequest(@RequestParam String requestId, @RequestParam boolean accept) {
        friendService.handleRequest(requestId, accept);
        return success();
    }

    @Operation(summary = "获取好友列表", description = "获取当前用户的好友列表")
    @GetMapping("/list")
    public Result<List<FriendDTO>> getFriends() {
        return success(friendService.getFriends());
    }

    @Operation(summary = "获取待处理请求", description = "获取别人发给我的待处理好友请求")
    @GetMapping("/pending")
    public Result<List<FriendDTO>> getPendingRequests() {
        return success(friendService.getPendingRequests());
    }

    @Operation(summary = "获取已发送请求", description = "获取我发出的待处理好友请求")
    @GetMapping("/sent")
    public Result<List<FriendDTO>> getSentRequests() {
        return success(friendService.getSentRequests());
    }

    @Operation(summary = "删除好友", description = "删除指定好友")
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@PathVariable String friendId) {
        friendService.deleteFriend(friendId);
        return success();
    }
}
