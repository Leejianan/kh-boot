package com.kh.boot.controller;

import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.entity.KhAdminAvatar;
import com.kh.boot.service.KhAdminAvatarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Avatar Management", description = "System Default Avatars")
@RestController
@RequestMapping("/admin/system/avatar")
public class KhAdminAvatarController extends BaseController {

    @Autowired
    private KhAdminAvatarService avatarService;

    @Operation(summary = "List All Avatars")
    @GetMapping("/list")
    public Result<List<KhAdminAvatar>> list() {
        return success(avatarService.list());
    }
}
