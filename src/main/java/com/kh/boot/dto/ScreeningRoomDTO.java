package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 放映室 DTO
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@Schema(description = "放映室详情")
public class ScreeningRoomDTO {

    @Schema(description = "放映室ID")
    private String id;

    @Schema(description = "房间名称")
    private String name;

    @Schema(description = "房主ID")
    private String ownerId;

    @Schema(description = "房主用户名")
    private String ownerUsername;

    @Schema(description = "房主名称")
    private String ownerName;

    @Schema(description = "房主头像")
    private String ownerAvatar;

    @Schema(description = "当前视频ID")
    private String currentVideoId;

    @Schema(description = "当前视频标题")
    private String currentVideoTitle;

    @Schema(description = "当前视频URL")
    private String currentVideoUrl;

    @Schema(description = "当前播放时间（秒）")
    private Integer currentTime;

    @Schema(description = "是否播放中")
    private Integer isPlaying;

    @Schema(description = "是否有密码")
    private Boolean hasPassword;

    @Schema(description = "是否曾经加入过（历史成员）")
    private Boolean isHistoryMember;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "在线人数")
    private Integer memberCount;

    @Schema(description = "成员列表")
    private List<RoomMemberDTO> members;

    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 房间成员 DTO
     */
    @Data
    @Schema(description = "房间成员")
    public static class RoomMemberDTO {
        @Schema(description = "用户ID")
        private String userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "真实姓名")
        private String realName;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "加入时间")
        private Date joinTime;
    }
}
