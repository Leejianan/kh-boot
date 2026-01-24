package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 放映室成员实体
 *
 * @author harlan
 * @since 2026-01-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fire_room_member")
@Schema(description = "放映室成员")
public class FireRoomMember extends KhBaseEntity {

    @Schema(description = "放映室ID")
    private String roomId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "加入时间")
    private Date joinTime;
}
