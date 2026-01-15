package com.kh.boot.controller;

import com.kh.boot.common.Result;
import com.kh.boot.converter.UserConverter;
import com.kh.boot.dto.KhUserInfoDTO;
import com.kh.boot.dto.KhUserRegisterDTO;
import com.kh.boot.entity.KhUser;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.service.UserService;
import com.kh.boot.util.EntityUtils;
import com.kh.boot.util.SecurityUtils;
import com.kh.boot.vo.KhRouterVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "Authentication", description = "User authentication APIs")
@RestController
@RequestMapping("/admin/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.kh.boot.service.SmsService smsService;

    @Autowired
    private com.kh.boot.service.EmailService emailService;

    @Value("${kh.security.rsa.private-key}")
    private String privateKey;

    @Operation(summary = "User Register", description = "Register a new user")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Validated KhUserRegisterDTO registerDTO) {
        KhUser user = userConverter.toEntity(registerDTO);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1); // Normal
        user.setAuditStatus(1); // Approved by default for simple scaffold

        EntityUtils.initInsert(user);

        // In production, should check if user exists
        boolean success = userService.save(user);

        if (success) {
            return Result.success("Register success", null);
        } else {
            return Result.error(500, "Register failed");
        }
    }

    @Operation(summary = "Send SMS Code", description = "Send verification code for SMS login")
    @PostMapping("/sms/code")
    public Result<String> sendSmsCode(@RequestParam String phone) {
        // Simple validation
        if (phone == null || phone.length() < 11) {
            return Result.error("Invalid phone number");
        }

        boolean success = smsService.sendCode(phone);
        if (success) {
            // In a real env, don't return code. For mock, we might want to hint it's in
            // logs.
            return Result.success("Code sent (check logs for mock)");
        } else {
            return Result.error("Failed to send code");
        }
    }

    @Operation(summary = "Send Email Code", description = "Send verification code for Email login")
    @PostMapping("/email/code")
    public Result<String> sendEmailCode(@RequestParam String email) {
        if (email == null || !email.contains("@")) {
            return Result.error("Invalid email address");
        }

        String code = emailService.sendCode(email);
        if (code != null) {
            return Result.success("Code sent to " + email);
        } else {
            return Result.error("Failed to send code");
        }
    }

    @Operation(summary = "Get User Info", description = "Get current logged-in user info and permissions")
    @GetMapping("/user/info")
    public Result<KhUserInfoDTO> info() {
        LoginUser loginUser = SecurityUtils.getLoginUser();

        if (loginUser == null) {
            return Result.error(401, "Unauthorized");
        }

        KhUserInfoDTO userInfoDTO = new KhUserInfoDTO();
        userInfoDTO.setId(loginUser.getUserId());
        userInfoDTO.setUsername(loginUser.getUsername());
        if (loginUser.getAvatar() == null) {
            userInfoDTO.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        } else {
            userInfoDTO.setAvatar(loginUser.getAvatar());
        }

        userInfoDTO.setRoles(Collections.singletonList("admin"));
        userInfoDTO.setPermissions(loginUser.getPermissions());

        return Result.success(userInfoDTO);
    }

    @Operation(summary = "Get User Menus", description = "Get dynamic menu tree for frontend")
    @GetMapping("/user/menus")
    public Result<List<KhRouterVo>> getMenus() {
        String userId = SecurityUtils.getUserId();

        if (userId == null) {
            return Result.error("User not found");
        }

        return Result.success(userService.getMenusByUserId(userId));
    }
}
