package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 弹幕 DTO
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@Schema(description = "弹幕信息")
public class DanmakuDTO {

    @Schema(description = "弹幕ID")
    private String id;

    @Schema(description = "放映室ID")
    private String roomId;

    @Schema(description = "视频ID")
    private String videoId;

    @Schema(description = "发送者ID")
    private String userId;

    @Schema(description = "发送者名称")
    private String username;

    @Schema(description = "发送者头像")
    private String avatar;

    @Schema(description = "弹幕内容")
    private String content;

    @Schema(description = "视频时间点（秒）")
    private Integer videoTime;

    @Schema(description = "弹幕颜色")
    private String color;

    @Schema(description = "位置")
    private String position;

    @Schema(description = "发送时间")
    private Date createTime;
}
