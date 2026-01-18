package com.kh.boot.controller;

import com.kh.boot.cache.AuthCache;
import com.kh.boot.common.Result;
import com.kh.boot.dto.*;
import java.util.stream.Collectors;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private AuthCache authCache;

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

    @Operation(summary = "Get User Info", description = "Get current logged-in user info, permissions and menus")
    @GetMapping("/user/info")
    @PreAuthorize("hasRole('ADMIN')")
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

        // Fetch real roles from DB
        String userId = loginUser.getUserId();
        List<com.kh.boot.entity.KhRole> roleList = userService.getRolesByUserId(userId);

        // If no roles found, fallback to empty list (or default role if logic requires)
        List<String> roleKeys = roleList.stream()
                .map(com.kh.boot.entity.KhRole::getRoleKey)
                .collect(Collectors.toList());
        List<String> roleNames = roleList.stream()
                .map(com.kh.boot.entity.KhRole::getName)
                .collect(Collectors.toList());

        userInfoDTO.setRoles(roleKeys);
        userInfoDTO.setRoleNames(roleNames);
        userInfoDTO.setPermissions(loginUser.getPermissions());

        // Get menus from cache, or load from DB if not cached
        // Reuse userId from above
        List<KhRouterVo> menus = authCache.getMenus(userId);
        if (menus == null) {
            menus = userService.getMenusByUserId(userId);
            authCache.putMenus(userId, menus);
        }
        userInfoDTO.setMenus(menus);

        return Result.success(userInfoDTO);
    }

    @Operation(summary = "Get User Menus", description = "Get dynamic menu tree for frontend (deprecated, use /user/info instead)")
    @GetMapping("/user/menus")
    @Deprecated
    public Result<List<KhRouterVo>> getMenus() {
        String userId = SecurityUtils.getUserId();

        if (userId == null) {
            return Result.error("未找到用户信息");
        }

        // Also use cache here for consistency
        List<KhRouterVo> menus = authCache.getMenus(userId);
        if (menus == null) {
            menus = userService.getMenusByUserId(userId);
            authCache.putMenus(userId, menus);
        }
        return Result.success(menus);
    }

    @Operation(summary = "Heartbeat Check", description = "Check if current user session is still valid")
    @GetMapping("/heartbeat")
    public Result<Boolean> heartbeat() {
        LoginUser loginUser = SecurityUtils.getLoginUser();

        if (loginUser == null) {
            return Result.success(false);
        }

        // Check if token is still in cache
        boolean isOnline = authCache.containsUser(loginUser.getUsername(), loginUser.getUserType());
        return Result.success(isOnline);
    }
}
