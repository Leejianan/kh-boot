package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 放映室实体
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fire_screening_room")
@Schema(description = "放映室")
public class FireScreeningRoom extends KhBaseEntity {

    @Schema(description = "房间名称")
    private String name;

    @Schema(description = "房主用户ID")
    private String ownerId;

    @Schema(description = "当前播放视频ID")
    private String currentVideoId;

    @Schema(description = "当前播放时间（秒）")
    @TableField("play_time")
    private Integer playTime;

    @Schema(description = "是否播放中 (1:播放, 0:暂停)")
    private Integer isPlaying;

    @Schema(description = "房间密码（可选）")
    private String password;

    @Schema(description = "状态 (1:开放, 0:关闭)")
    private Integer status;
}
