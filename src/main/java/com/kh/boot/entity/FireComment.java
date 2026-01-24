package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fire_comment")
@Schema(description = "评论")
public class FireComment extends KhBaseEntity {

    @Schema(description = "放映室ID")
    private String roomId;

    @Schema(description = "视频ID")
    private String videoId;

    @Schema(description = "评论者ID")
    private String userId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private String parentId;
}
