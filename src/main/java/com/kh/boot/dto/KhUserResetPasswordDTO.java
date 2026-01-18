package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "User Reset Password DTO")
public class KhUserResetPasswordDTO {

    @NotBlank(message = "User ID cannot be empty")
    @Schema(description = "User ID")
    private String id;

    @NotBlank(message = "Password cannot be empty")
    @Schema(description = "New Password (Encrypted)")
    private String password;
}
