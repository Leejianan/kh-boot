package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 弹幕实体
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fire_danmaku")
@Schema(description = "弹幕")
public class FireDanmaku extends KhBaseEntity {

    @Schema(description = "放映室ID")
    private String roomId;

    @Schema(description = "视频ID")
    private String videoId;

    @Schema(description = "发送者ID")
    private String userId;

    @Schema(description = "弹幕内容")
    private String content;

    @Schema(description = "视频时间点（秒）")
    private Integer videoTime;

    @Schema(description = "弹幕颜色")
    private String color;

    @Schema(description = "位置 (scroll/top/bottom)")
    private String position;
}
