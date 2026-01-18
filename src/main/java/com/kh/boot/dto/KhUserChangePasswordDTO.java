package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "User Password Change DTO")
public class KhUserChangePasswordDTO {

    @Schema(description = "Old Password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @Schema(description = "New Password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New password is required")
    private String newPassword;
}
