package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Email Login Request Parameter")
public class KhEmailLoginRequest {

    @Schema(description = "Email Address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Verification Code", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
