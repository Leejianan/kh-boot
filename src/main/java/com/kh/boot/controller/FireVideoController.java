package com.kh.boot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kh.boot.common.Result;
import com.kh.boot.entity.FireVideo;
import com.kh.boot.service.FireVideoService;
import com.kh.boot.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 视频管理控制器
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@RestController
@RequestMapping("/admin/video")
@RequiredArgsConstructor
@Tag(name = "视频管理", description = "视频 CRUD 接口")
public class FireVideoController {

    private final FireVideoService videoService;

    @GetMapping("/list")
    @Operation(summary = "视频列表", description = "分页获取视频列表")
    public Result<IPage<FireVideo>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category) {
        return Result.success(videoService.getVideoList(current, size, title, category));
    }

    @GetMapping("/{id}")
    @Operation(summary = "视频详情", description = "获取视频详情")
    public Result<FireVideo> detail(@PathVariable String id) {
        return Result.success(videoService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('screening:video:add')")
    @Operation(summary = "添加视频", description = "添加新视频")
    public Result<FireVideo> add(@RequestBody FireVideo video) {
        video.setStatus(1);
        video.setCreateBy(SecurityUtils.getUserId());
        video.setCreateByName(SecurityUtils.getUsername());
        video.setCreateTime(new Date());
        videoService.save(video);
        return Result.success(video);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('screening:video:edit')")
    @Operation(summary = "编辑视频", description = "更新视频信息")
    public Result<Void> update(@PathVariable String id, @RequestBody FireVideo video) {
        video.setId(id);
        video.setUpdateBy(SecurityUtils.getUserId());
        video.setUpdateByName(SecurityUtils.getUsername());
        video.setUpdateTime(new Date());
        videoService.updateById(video);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('screening:video:delete')")
    @Operation(summary = "删除视频", description = "删除视频")
    public Result<Void> delete(@PathVariable String id) {
        videoService.removeById(id);
        return Result.success();
    }
}
