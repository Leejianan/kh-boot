package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评论 DTO
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@Schema(description = "评论信息")
public class CommentDTO {

    @Schema(description = "评论ID")
    private String id;

    @Schema(description = "放映室ID")
    private String roomId;

    @Schema(description = "视频ID")
    private String videoId;

    @Schema(description = "评论者ID")
    private String userId;

    @Schema(description = "评论者名称")
    private String username;

    @Schema(description = "评论者真实姓名")
    private String realName;

    @Schema(description = "评论者头像")
    private String avatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private String parentId;

    @Schema(description = "子评论列表")
    private List<CommentDTO> replies;

    @Schema(description = "评论时间")
    private Date createTime;
}
