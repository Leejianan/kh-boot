package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "User Registration Request")
public class KhUserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Username", example = "new_user", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Phone Number", example = "13800001111")
    private String phone;

    @Schema(description = "Email", example = "user@example.com")
    private String email;
}
