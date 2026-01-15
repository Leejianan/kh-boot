package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * Email Sending Record DTO
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Data
@Schema(description = "Email Sending Record DTO")
public class KhEmailRecordDTO implements Serializable {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "Recipient Email")
    private String sendTo;

    @Schema(description = "Email Subject")
    private String subject;

    @Schema(description = "Email Content")
    private String content;

    @Schema(description = "Sending Result: 1-Success, 0-Fail")
    private Integer sendResult;

    @Schema(description = "Failure Reason")
    private String failReason;

    @Schema(description = "Create Time")
    private LocalDateTime createTime;
}
