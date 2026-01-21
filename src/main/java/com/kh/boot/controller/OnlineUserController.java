package com.kh.boot.controller;

import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.dto.KhOnlineUserDTO;
import com.kh.boot.cache.AuthCache;
import com.kh.boot.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Online User Management", description = "Monitor and manage online users")
@RestController
@RequestMapping("/admin/system/online")
public class OnlineUserController extends BaseController {

    @Autowired
    private AuthCache authCache;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "List Online Users", description = "Get details of currently logged-in users with pagination")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:online:list')")
    public Result<PageData<KhOnlineUserDTO>> list(com.kh.boot.query.BaseQuery query) {
        return success(authCache.pageOnlineUsers(query.getCurrent(), query.getSize()));
    }

    @Operation(summary = "Force Logout User", description = "Invalidate user token and force logout")
    @DeleteMapping("/{username}")
    @PreAuthorize("hasAuthority('system:online:logout')")
    public Result<Void> logout(@PathVariable String username, @RequestParam String userType) {
        notificationService.sendKickOut(username, userType, null, "您的账号已被管理员强制下线。");
        authCache.remove(username, userType);
        return success();
    }
}
