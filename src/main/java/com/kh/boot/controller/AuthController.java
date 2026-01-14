package com.kh.boot.controller;

import com.kh.boot.common.Result;
import com.kh.boot.converter.UserConverter;
import com.kh.boot.dto.KhLoginRequest;
import com.kh.boot.dto.KhOnlineUserDTO;
import com.kh.boot.dto.KhUserInfoDTO;
import com.kh.boot.dto.KhUserRegisterDTO;
import com.kh.boot.entity.KhUser;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.service.UserService;
import com.kh.boot.util.EntityUtils;
import com.kh.boot.util.JwtUtil;
import com.kh.boot.util.SecurityUtils;
import com.kh.boot.vo.KhRouterVo;
import com.kh.boot.cache.AuthCache;
import eu.bitwalker.useragentutils.UserAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Tag(name = "Authentication", description = "User authentication APIs")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private AuthCache authCache;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${kh.security.rsa.private-key}")
    private String privateKey;

    @Operation(summary = "User Login", description = "Login with username and password to get JWT token")
    @PostMapping("/login")
    public Result<String> login(@RequestBody KhLoginRequest loginRequest, HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // Decrypt password if it's RSA encrypted
        try {
            password = com.kh.boot.util.RsaUtils.decrypt(password, privateKey);
        } catch (Exception e) {
            // If decryption fails, we assume it's either plaintext (for dev)
            // or a real error. For now, we'll let it fall through
            // but in production you'd likely throw an error.
        }

        KhUser user = userService.findByUsername(username);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken(username, "admin");

            // Build LoginUser with rich metadata
            LoginUser loginUser = new LoginUser(user, userService.getPermissionsByUserId(user.getId()));
            long nowTime = System.currentTimeMillis();
            loginUser.setLoginTime(new Date(nowTime));
            loginUser.setExpireTime(nowTime + 1000 * 60 * 60 * 2); // 2 Hours
            loginUser.setIpaddr(request.getRemoteAddr());
            loginUser.setLoginLocation("Local Host");
            loginUser.setUserType("admin");

            // Parse User-Agent
            String uaStr = request.getHeader("User-Agent");
            UserAgent ua = UserAgent.parseUserAgentString(uaStr);
            loginUser.setBrowser(ua.getBrowser().getName());
            loginUser.setOs(ua.getOperatingSystem().getName());

            // 1. Cache token
            authCache.putToken(username, "admin", token);

            // 2. Cache LoginUser object
            authCache.putUser(username, "admin", loginUser);

            // 3. Track online status
            KhOnlineUserDTO onlineUser = new KhOnlineUserDTO();
            onlineUser.setUsername(username);
            onlineUser.setUserType("admin");
            onlineUser.setIp(loginUser.getIpaddr());
            onlineUser.setLoginTime(loginUser.getLoginTime());
            onlineUser.setBrowser(loginUser.getBrowser());
            onlineUser.setOs(loginUser.getOs());
            onlineUser.setToken(token.substring(0, Math.min(token.length(), 20)) + "...");
            authCache.putOnlineUser(onlineUser);

            return Result.success("Login success", token);
        } else {
            return Result.error(401, "Invalid username or password");
        }
    }

    @Operation(summary = "User Register", description = "Register a new user")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody KhUserRegisterDTO registerDTO) {
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

    @Operation(summary = "Get User Info", description = "Get current logged-in user info and permissions")
    @GetMapping("/user/info")
    public Result<KhUserInfoDTO> info() {
        LoginUser loginUser = SecurityUtils.getLoginUser();

        if (loginUser == null) {
            return Result.error(401, "Unauthorized");
        }

        KhUser user = loginUser.getUser();
        KhUserInfoDTO userInfoDTO = new KhUserInfoDTO();
        userInfoDTO.setId(user.getId());
        userInfoDTO.setUsername(user.getUsername());
        userInfoDTO.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");

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
