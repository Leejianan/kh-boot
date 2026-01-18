package com.kh.boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "User Profile Update DTO")
public class KhUserProfileDTO {

    @Schema(description = "Real Name")
    @Size(max = 50, message = "Real name cannot exceed 50 characters")
    private String realName;

    @Schema(description = "Gender (0: Unknown, 1: Male, 2: Female)")
    private Integer gender;

    @Schema(description = "Phone Number")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "Invalid phone number format")
    private String phone;

    @Schema(description = "Email Address")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Avatar Path")
    private String avatar;
}
