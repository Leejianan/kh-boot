package com.kh.boot.controller;

import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.dto.KhUserChangePasswordDTO;
import com.kh.boot.dto.KhUserProfileDTO;
import com.kh.boot.entity.KhUser;
import com.kh.boot.service.UserService;
import com.kh.boot.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile Management", description = "User Self-Service APIs")
@RestController
@RequestMapping("/admin/profile")
public class ProfileController extends BaseController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get Profile Info")
    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<KhUser> getProfile() {
        // Fetch fresh user data
        KhUser user = userService.getById(SecurityUtils.getUserId());
        // Mask password for safety
        user.setPassword(null);
        return success(user);
    }

    @Operation(summary = "Update Profile Info")
    @PutMapping("/info")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateProfile(@RequestBody @Validated KhUserProfileDTO profileDTO) {
        String userId = SecurityUtils.getUserId();
        userService.updateProfile(userId, profileDTO);
        return success(null);
    }

    @Operation(summary = "Change Password")
    @PutMapping("/password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> changePassword(@RequestBody @Validated KhUserChangePasswordDTO passwordDTO) {
        String userId = SecurityUtils.getUserId();
        userService.changePassword(userId, passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return success(null);
    }
}
