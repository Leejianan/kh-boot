package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友关系 DTO
 *
 * @author harlan
 * @since 2024-01-20
 */
@Data
@Schema(description = "好友关系 DTO")
public class FriendDTO implements Serializable {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "好友ID")
    private String friendId;

    @Schema(description = "好友用户名")
    private String friendUsername;

    @Schema(description = "好友邮箱")
    private String friendEmail;

    @Schema(description = "好友手机号")
    private String friendPhone;

    @Schema(description = "状态: 0-待确认, 1-已同意, 2-已拒绝")
    private Integer status;

    @Schema(description = "好友真实姓名")
    private String friendRealName;

    @Schema(description = "好友头像")
    private String friendAvatar;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "申请时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
