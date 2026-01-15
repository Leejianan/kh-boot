package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SMS Login Request Parameter")
public class KhSmsLoginRequest {

    @Schema(description = "Phone Number", example = "13800001111", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Verification Code", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
