package com.kh.boot.dto;

import com.kh.boot.vo.KhRouterVo;
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

    @Schema(description = "Real Name", example = "John Doe")
    private String realName;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "Role List (Keys)", example = "[\"admin\"]")
    private List<String> roles;

    @Schema(description = "Role Name List (Display)", example = "[\"管理员\"]")
    private List<String> roleNames;

    @Schema(description = "Permission List", example = "[\"user:add\", \"user:edit\"]")
    private List<String> permissions;

    @Schema(description = "Menu Tree for frontend routing")
    private List<KhRouterVo> menus;
}
