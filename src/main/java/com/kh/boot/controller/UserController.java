package com.kh.boot.controller;

import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.dto.KhUserDTO;
import com.kh.boot.service.UserService;
import com.kh.boot.query.UserQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get User List", description = "Retrieve user list with pagination")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageData<KhUserDTO>> getUserList(UserQuery query) {
        return pageSuccess(userService.page(query));
    }

    @Operation(summary = "Assign Roles to User")
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Boolean> assignRoles(@PathVariable String id, @RequestBody List<String> roleIds) {
        userService.assignRoles(id, roleIds);
        return success(true);
    }

    @Operation(summary = "Get User Role IDs")
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<List<String>> getRoles(@PathVariable String id) {
        return success(userService.getRoleIdsByUserId(id));
    }
}
