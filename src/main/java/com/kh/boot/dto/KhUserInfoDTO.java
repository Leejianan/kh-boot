package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "User Info DTO")
public class KhUserInfoDTO {

    @Schema(description = "User ID", example = "1")
    private String id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "Role List", example = "[\"admin\"]")
    private List<String> roles;

    @Schema(description = "Permission List", example = "[\"user:add\", \"user:edit\"]")
    private List<String> permissions;
}
