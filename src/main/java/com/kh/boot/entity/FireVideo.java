package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频信息实体
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fire_video")
@Schema(description = "视频信息")
public class FireVideo extends KhBaseEntity {

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "封面图路径")
    private String coverUrl;

    @Schema(description = "视频文件路径")
    private String videoUrl;

    @Schema(description = "时长（秒）")
    private Integer duration;

    @Schema(description = "视频描述")
    private String description;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "状态 (1:正常, 0:禁用)")
    private Integer status;
}
