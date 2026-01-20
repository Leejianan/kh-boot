package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 好友关系实体
 *
 * @author harlan
 * @since 2024-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_friend")
@Schema(name = "KhFriend", description = "好友关系")
public class KhFriend extends KhBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "发起方用户ID")
    private String userId;

    @Schema(description = "被添加方用户ID")
    private String friendId;

    @Schema(description = "状态: 0-待确认, 1-已同意, 2-已拒绝")
    private Integer status;

    // 状态常量
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_REJECTED = 2;
}
