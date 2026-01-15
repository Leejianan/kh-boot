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
@RequestMapping("/admin/system/user")
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

    @Operation(summary = "Create User")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Void> create(
            @RequestBody @org.springframework.validation.annotation.Validated com.kh.boot.dto.KhUserCreateDTO createDTO) {
        userService.createUser(createDTO);
        return success(null);
    }

    @Operation(summary = "Update User")
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> update(
            @RequestBody @org.springframework.validation.annotation.Validated com.kh.boot.dto.KhUserUpdateDTO updateDTO) {
        userService.updateUser(updateDTO);
        return success(null);
    }

    @Operation(summary = "Delete User")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return success(null);
    }
}
