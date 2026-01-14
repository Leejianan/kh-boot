package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

import java.io.Serializable;

@Data
@Schema(description = "Online User Information")
public class KhOnlineUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "User Type (admin/member)")
    private String userType;

    @Schema(description = "Client IP Address")
    private String ip;

    @Schema(description = "Login Time")
    private Date loginTime;

    @Schema(description = "Token (Truncated for security)")
    private String token;

    @Schema(description = "Browser")
    private String browser;

    @Schema(description = "Operating System")
    private String os;
}
