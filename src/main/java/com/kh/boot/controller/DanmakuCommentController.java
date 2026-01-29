package com.kh.boot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kh.boot.common.Result;
import com.kh.boot.dto.CommentDTO;
import com.kh.boot.dto.DanmakuDTO;
import com.kh.boot.service.CommentService;
import com.kh.boot.service.DanmakuService;
import com.kh.boot.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 弹幕和评论控制器
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@RestController
@RequestMapping("/admin/screening")
@RequiredArgsConstructor
@Tag(name = "弹幕评论", description = "弹幕和评论相关接口")
public class DanmakuCommentController {

    private final DanmakuService danmakuService;
    private final CommentService commentService;

    // ========== 弹幕接口 ==========

    @GetMapping("/danmaku/{videoId}")
    @Operation(summary = "获取弹幕", description = "获取视频的所有弹幕")
    public Result<List<DanmakuDTO>> getDanmaku(@PathVariable String videoId) {
        return Result.success(danmakuService.getDanmakuList(videoId));
    }

    @GetMapping("/danmaku/{videoId}/range")
    @Operation(summary = "获取时间段弹幕", description = "获取指定时间段的弹幕")
    public Result<List<DanmakuDTO>> getDanmakuByRange(
            @PathVariable String videoId,
            @RequestParam int startTime,
            @RequestParam int endTime) {
        return Result.success(danmakuService.getDanmakuByTimeRange(videoId, startTime, endTime));
    }

    @PostMapping("/danmaku")
    @Operation(summary = "发送弹幕", description = "发送弹幕")
    public Result<DanmakuDTO> sendDanmaku(@RequestBody Map<String, Object> body) {
        String roomId = (String) body.get("roomId");
        String videoId = (String) body.get("videoId");
        String content = (String) body.get("content");
        Integer videoTime = (Integer) body.get("videoTime");
        String color = (String) body.get("color");
        String position = (String) body.get("position");

        String userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        return Result.success(danmakuService.sendDanmaku(
                userId, username, roomId, videoId, content, videoTime != null ? videoTime : 0, color, position));
    }

    // ========== 评论接口 ==========

    @GetMapping("/comment/{videoId}")
    @Operation(summary = "获取评论", description = "分页获取视频评论")
    public Result<IPage<CommentDTO>> getComments(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.getCommentList(videoId, current, size));
    }

    @GetMapping("/room/comment/{roomId}")
    @Operation(summary = "获取房间评论", description = "分页获取房间评论")
    public Result<IPage<CommentDTO>> getRoomComments(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.getCommentListByRoom(roomId, current, size));
    }

    @PostMapping("/comment")
    @Operation(summary = "发表评论", description = "发表评论")
    public Result<CommentDTO> addComment(@RequestBody Map<String, String> body) {
        String roomId = body.get("roomId");
        String videoId = body.get("videoId");
        String content = body.get("content");
        String parentId = body.get("parentId");

        return Result.success(commentService.addComment(roomId, videoId, content, parentId));
    }

    @DeleteMapping("/comment/{id}")
    @Operation(summary = "删除评论", description = "删除自己的评论")
    public Result<Void> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
