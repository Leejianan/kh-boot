package com.kh.boot.controller;

import com.kh.boot.entity.FireVideo;
import com.kh.boot.service.FireVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 视频流服务控制器
 * 提供本地视频文件的 HTTP 流媒体服务
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@RestController
@RequestMapping("/admin/video")
@RequiredArgsConstructor
@Tag(name = "视频流服务", description = "视频流媒体播放接口")
public class VideoStreamController {

    private final FireVideoService videoService;

    /**
     * 视频流播放
     * 支持 Range 请求实现视频拖拽播放
     */
    @GetMapping("/stream/{videoId}")
    @Operation(summary = "视频流播放", description = "流式播放视频文件")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        FireVideo video = videoService.getById(videoId);
        if (video == null) {
            log.warn("Video not found, videoId={}", videoId);
            return ResponseEntity.notFound().build();
        }

        String rawPath = video.getVideoUrl();
        if (rawPath == null || rawPath.isBlank()) {
            log.error("Video videoUrl is empty, videoId={}, title={}", videoId, video.getTitle());
            return ResponseEntity.notFound().build();
        }

        String videoPath = resolveVideoPath(rawPath);
        log.debug("Video path resolved: '{}' -> '{}'", rawPath, videoPath);

        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            String home = System.getProperty("user.home");
            log.error("Video file not found. raw='{}' resolved='{}' user.home='{}' videoId={}",
                    rawPath, videoPath, home, videoId);
            return ResponseEntity.notFound().build();
        }

        try {
            long fileLength = videoFile.length();
            String contentType = Files.probeContentType(Paths.get(videoPath));
            if (contentType == null) {
                contentType = "video/mp4";
            }

            // 支持 Range 请求（视频拖拽）
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty()
                        ? Long.parseLong(ranges[1])
                        : fileLength - 1;

                if (start >= fileLength) {
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                            .build();
                }

                long contentLength = end - start + 1;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentLength(contentLength);
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);
                headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

                // 使用 InputStreamResource 支持范围请求
                java.io.RandomAccessFile raf = new java.io.RandomAccessFile(videoFile, "r");
                raf.seek(start);
                byte[] buffer = new byte[(int) contentLength];
                raf.readFully(buffer);
                raf.close();

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new org.springframework.core.io.ByteArrayResource(buffer));
            }

            // 完整文件响应
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileLength);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

            Resource resource = new FileSystemResource(videoFile);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error streaming video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 解析视频文件路径。
     * - ~ 或 ~/ 开头：替换为 user.home（或环境变量 HOME）
     * - 其他：原样返回
     */
    private String resolveVideoPath(String path) {
        if (path == null || path.isBlank()) return path;
        path = path.trim();
        if (!path.startsWith("~")) return path;
        String home = System.getProperty("user.home");
        if (home == null || home.isEmpty()) {
            home = System.getenv("HOME");
        }
        if (home == null) home = "";
        if (path.equals("~")) return home;
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            return home + path.substring(1);
        }
        return home + path.substring(1);
    }
}
