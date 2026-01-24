package com.kh.boot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kh.boot.common.Result;
import com.kh.boot.dto.ScreeningRoomDTO;
import com.kh.boot.entity.FireScreeningRoom;
import com.kh.boot.service.ScreeningRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 放映室控制器
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@RestController
@RequestMapping("/admin/room")
@RequiredArgsConstructor
@Tag(name = "放映室管理", description = "放映室相关接口")
public class ScreeningRoomController {

    private final ScreeningRoomService roomService;

    @GetMapping("/list")
    @Operation(summary = "放映室列表", description = "分页获取放映室列表")
    public Result<IPage<ScreeningRoomDTO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        return Result.success(roomService.getRoomList(current, size, name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "放映室详情", description = "获取放映室详情（含成员列表）")
    public Result<ScreeningRoomDTO> detail(@PathVariable String id) {
        return Result.success(roomService.getRoomDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('screening:room:create')")
    @Operation(summary = "创建放映室", description = "创建新的放映室")
    public Result<FireScreeningRoom> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String password = body.get("password");
        return Result.success(roomService.createRoom(name, password));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasAuthority('screening:room:join')")
    @Operation(summary = "加入放映室", description = "加入指定放映室")
    public Result<Void> join(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String password = body != null ? body.get("password") : null;
        roomService.joinRoom(id, password);
        return Result.success();
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "离开放映室", description = "离开当前放映室")
    public Result<Void> leave(@PathVariable String id) {
        roomService.leaveRoom(id);
        return Result.success();
    }

    @PutMapping("/{id}/video")
    @Operation(summary = "切换视频", description = "房主切换当前播放视频")
    public Result<Void> switchVideo(@PathVariable String id, @RequestBody Map<String, String> body) {
        String videoId = body.get("videoId");
        roomService.switchVideo(id, videoId);
        return Result.success();
    }

    @PutMapping("/{id}/sync")
    @Operation(summary = "同步播放状态", description = "同步播放进度和状态")
    public Result<Void> syncStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Boolean isPlaying = (Boolean) body.get("isPlaying");
        Integer currentTime = (Integer) body.get("currentTime");
        roomService.syncPlayStatus(id, isPlaying != null && isPlaying, currentTime != null ? currentTime : 0);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "关闭放映室", description = "房主关闭放映室")
    public Result<Void> close(@PathVariable String id) {
        roomService.closeRoom(id);
        return Result.success();
    }
}
