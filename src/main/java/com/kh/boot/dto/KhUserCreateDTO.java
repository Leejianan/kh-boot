package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "Admin User Creation DTO")
public class KhUserCreateDTO implements Serializable {

    @Schema(description = "Username")
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Schema(description = "Password")
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Schema(description = "Phone Number")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Status: 1-Normal, 0-Disabled")
    private Integer status = 1;

    @Schema(description = "Role IDs to assign")
    private List<String> roleIds;
}
