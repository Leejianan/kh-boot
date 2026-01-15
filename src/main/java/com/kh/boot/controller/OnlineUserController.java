package com.kh.boot.controller;

import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.dto.KhOnlineUserDTO;
import com.kh.boot.cache.AuthCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Online User Management", description = "Monitor and manage online users")
@RestController
@RequestMapping("/online")
public class OnlineUserController extends BaseController {

    @Autowired
    private AuthCache authCache;

    @Operation(summary = "List Online Users", description = "Get details of all currently logged-in users")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:online:list')")
    public Result<List<KhOnlineUserDTO>> list() {
        return success(authCache.listOnlineUsers());
    }

    @Operation(summary = "Force Logout User", description = "Invalidate user token and force logout")
    @DeleteMapping("/{username}")
    @PreAuthorize("hasAuthority('system:online:logout')")
    public Result<Void> logout(@PathVariable String username, @RequestParam String userType) {
        authCache.remove(username, userType);
        return success();
    }
}
