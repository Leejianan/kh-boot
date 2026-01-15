package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "Admin User Update DTO")
public class KhUserUpdateDTO implements Serializable {

    @Schema(description = "User ID")
    @NotBlank(message = "User ID cannot be empty")
    private String id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Phone Number")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Status: 1-Normal, 0-Disabled")
    private Integer status;

    @Schema(description = "Role IDs to assign (null to keep existing)")
    private List<String> roleIds;

    @Schema(description = "New Password (leave empty to keep existing)")
    private String password;
}
