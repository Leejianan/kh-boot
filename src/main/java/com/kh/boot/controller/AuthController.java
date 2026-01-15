package com.kh.boot.controller;

import com.kh.boot.common.Result;
import com.kh.boot.dto.*;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.service.EmailService;
import com.kh.boot.service.SmsService;
import com.kh.boot.service.UserService;
import com.kh.boot.util.SecurityUtils;
import com.kh.boot.vo.KhRouterVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @Value("${kh.security.rsa.private-key}")
    private String privateKey;

    @Operation(summary = "User Login", description = "Login with username and password (JSON)")
    @PostMapping("/login")
    public Result<String> login(@RequestBody KhLoginRequest loginRequest) {
        // Handled by Security Filter
        return Result.success("Login Success", null);
    }

    @Operation(summary = "SMS Login", description = "Login with phone and code (Form Data)")
    @PostMapping("/login/sms")
    public Result<String> smsLogin(@RequestParam String phone, @RequestParam String code) {
        // Handled by Security Filter
        return Result.success("Login Success", null);
    }

    @Operation(summary = "Email Login", description = "Login with email and code (Form Data)")
    @PostMapping("/login/email")
    public Result<String> emailLogin(@RequestParam String email, @RequestParam String code) {
        // Handled by Security Filter
        return Result.success("Login Success", null);
    }

    @Operation(summary = "Send SMS Code", description = "Send verification code for SMS login")
    @PostMapping("/sms/code")
    public Result<String> sendSmsCode(@RequestParam String phone) {
        // Simple validation
        if (phone == null || phone.length() < 11) {
            return Result.error("手机号无效");
        }

        boolean success = smsService.sendCode(phone);
        if (success) {
            // In a real env, don't return code. For mock, we might want to hint it's in
            // logs.
            return Result.success("验证码已发送 (请查看日志)", null);
        } else {
            return Result.error("验证码发送失败");
        }
    }

    @Operation(summary = "Send Email Code", description = "Send verification code for Email login")
    @PostMapping("/email/code")
    public Result<String> sendEmailCode(@RequestParam String email) {
        if (email == null || !email.contains("@")) {
            return Result.error("邮箱地址无效");
        }

        String code = emailService.sendCode(email);
        if (code != null) {
            return Result.success("验证码已发送至 " + email);
        } else {
            return Result.error("验证码发送失败");
        }
    }

    @Operation(summary = "Get User Info", description = "Get current logged-in user info and permissions")
    @GetMapping("/user/info")
    public Result<KhUserInfoDTO> info() {
        LoginUser loginUser = SecurityUtils.getLoginUser();

        if (loginUser == null) {
            return Result.error(401, "未授权");
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
            return Result.error("未找到用户信息");
        }

        return Result.success(userService.getMenusByUserId(userId));
    }
}
