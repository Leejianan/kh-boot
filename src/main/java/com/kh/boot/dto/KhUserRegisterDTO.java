package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "User Registration Request")
public class KhUserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Username", example = "new_user", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 4, max = 30, message = "Username must be between 4 and 30 characters")
    private String username;

    @Schema(description = "Password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 32, message = "Password must be between 6 and 32 characters")
    private String password;

    @Schema(description = "Phone Number", example = "13800001111")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number format")
    private String phone;

    @Schema(description = "Email", example = "user@example.com")
    @Email(message = "Invalid email format")
    private String email;
}
