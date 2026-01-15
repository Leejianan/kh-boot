package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>
 * Email Sending Record
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_email_record")
@Schema(name = "KhEmailRecord", description = "Email Sending Record")
public class KhEmailRecord extends KhBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收件人邮箱")
    private String sendTo;

    @Schema(description = "邮件主题")
    private String sendSubject;

    @Schema(description = "邮件内容")
    private String sendContent;

    @Schema(description = "发送结果: 1-成功, 0-失败")
    private Integer sendResult;

    @Schema(description = "失败原因")
    private String failReason;
}
