package com.kh.boot.controller;

import com.kh.boot.entity.FireVideo;
import com.kh.boot.service.FireVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 视频流服务控制器
 * 提供本地视频文件的 HTTP 流媒体服务
 * 
 * 开发环境：Java 直接流式传输视频
 * 生产环境：使用 Nginx X-Accel-Redirect 减轻 Java 服务器压力
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
     * 是否使用 Nginx X-Accel-Redirect
     * 开发环境：false (Java 直接传输)
     * 生产环境：true (Nginx 接管传输)
     */
    @Value("${video.use-nginx-accel:false}")
    private boolean useNginxAccel;

    /**
     * 视频流播放
     * 开发环境：Java 直接流式传输，支持 HTTP Range 请求
     * 生产环境：使用 Nginx X-Accel-Redirect，由 Nginx 接管文件传输
     */
    @GetMapping("/stream/{videoId}")
    @Operation(summary = "视频流播放", description = "流式播放视频文件，支持断点续传")
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
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            String home = System.getProperty("user.home");
            log.error("Video file not found. raw='{}' resolved='{}' user.home='{}' videoId={}",
                    rawPath, videoPath, home, videoId);
            return ResponseEntity.notFound().build();
        }

        try {
            String contentType = Files.probeContentType(Paths.get(videoPath));
            if (contentType == null) {
                contentType = "video/mp4";
            }

            long fileSize = videoFile.length();

            // 生产环境：使用 Nginx X-Accel-Redirect
            if (useNginxAccel) {
                log.debug("Using Nginx X-Accel-Redirect for video: {}", videoPath);
                return handleNginxAccelRedirect(videoPath, contentType, fileSize);
            }

            // 开发环境：Java 直接流式传输
            // 开发环境：Java 直接流式传输
            log.debug("Using Java stream for video: {}", videoPath);
            return handleJavaStream(videoFile, contentType, fileSize, rangeHeader);

        } catch (IOException e) {
            log.error("Error streaming video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 生产环境：使用 Nginx X-Accel-Redirect
     * 返回特殊响应头，让 Nginx 接管文件传输
     */
    private ResponseEntity<Resource> handleNginxAccelRedirect(String videoPath, String contentType, long fileSize) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        // headers.setContentLength(fileSize); // Remove this: let Nginx handle
        // Content-Length

        // X-Accel-Redirect: 告诉 Nginx 从 /internal_video/ 路径读取文件
        // Nginx 配置中 alias / 会将 /internal_video/xxx 映射到 /xxx
        headers.set("X-Accel-Redirect", "/internal_video" + videoPath);

        // X-Accel-Buffering: 关闭 Nginx 缓冲，实现真正的流式传输
        headers.set("X-Accel-Buffering", "no");

        log.info("Nginx X-Accel-Redirect: {} -> /internal_video{}", videoPath, videoPath);

        // 返回空 body，Nginx 会接管后续传输
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    /**
     * 开发环境：Java 直接流式传输
     * 支持 HTTP Range 请求
     */
    private ResponseEntity<Resource> handleJavaStream(File videoFile, String contentType, long fileSize,
            String rangeHeader) throws IOException {
        // 处理 Range 请求
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty()
                    ? Long.parseLong(ranges[1])
                    : fileSize - 1;

            if (start >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            long contentLength = end - start + 1;

            log.debug("Range request: bytes={}-{}/{}", start, end, fileSize);

            // 使用 RandomAccessFile 读取指定范围的数据
            RandomAccessFile randomAccessFile = new RandomAccessFile(videoFile, "r");
            randomAccessFile.seek(start);

            InputStreamResource resource = new InputStreamResource(new java.io.InputStream() {
                private long bytesRead = 0;

                @Override
                public int read() throws IOException {
                    if (bytesRead >= contentLength) {
                        return -1;
                    }
                    bytesRead++;
                    return randomAccessFile.read();
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    if (bytesRead >= contentLength) {
                        return -1;
                    }
                    long remaining = contentLength - bytesRead;
                    int toRead = (int) Math.min(len, remaining);
                    int actualRead = randomAccessFile.read(b, off, toRead);
                    if (actualRead > 0) {
                        bytesRead += actualRead;
                    }
                    return actualRead;
                }

                @Override
                public void close() throws IOException {
                    randomAccessFile.close();
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
            headers.setContentLength(contentLength);

            return new ResponseEntity<>(resource, headers, HttpStatus.PARTIAL_CONTENT);
        }

        // 没有 Range 请求，返回完整文件
        InputStreamResource resource = new InputStreamResource(new java.io.FileInputStream(videoFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.setContentLength(fileSize);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * 解析视频文件路径。
     * - ~ 或 ~/ 开头：替换为 user.home（或环境变量 HOME）
     * - 其他：原样返回
     */
    private String resolveVideoPath(String path) {
        if (path == null || path.isBlank())
            return path;
        path = path.trim();
        if (!path.startsWith("~"))
            return path;
        String home = System.getProperty("user.home");
        if (home == null || home.isEmpty()) {
            home = System.getenv("HOME");
        }
        if (home == null)
            home = "";
        if (path.equals("~"))
            return home;
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            return home + path.substring(1);
        }
        return home + path.substring(1);
    }
}
