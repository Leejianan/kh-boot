package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 聊天消息实体
 *
 * @author harlan
 * @since 2024-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_chat_message")
@Schema(name = "KhChatMessage", description = "聊天消息")
public class KhChatMessage extends KhBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "发送者ID")
    private String senderId;

    @Schema(description = "接收者ID")
    private String receiverId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型: 0-文本, 1-图片, 2-文件")
    private Integer msgType;

    @Schema(description = "是否已读: 0-未读, 1-已读")
    private Integer isRead;

    // 消息类型常量
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_FILE = 2;
}
