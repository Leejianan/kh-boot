package com.kh.boot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kh.boot.common.Result;
import com.kh.boot.dto.KhRoleDTO;
import com.kh.boot.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Role Management")
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "Get Role Page")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<IPage<KhRoleDTO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        return Result.success(roleService.getRolePage(new Page<>(pageNum, pageSize), name));
    }

    @Operation(summary = "Add Role")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<?> add(@RequestBody KhRoleDTO roleDTO) {
        roleService.addRole(roleDTO);
        return Result.success();
    }

    @Operation(summary = "Update Role")
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<?> update(@RequestBody KhRoleDTO roleDTO) {
        roleService.updateRole(roleDTO);
        return Result.success();
    }

    @Operation(summary = "Delete Role")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<?> delete(@PathVariable String id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "Assign Permissions to Role")
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:assign')")
    public Result<?> assignPermissions(@PathVariable String id, @RequestBody List<String> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    @Operation(summary = "Get Role Permission IDs")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<String>> getRolePermissions(@PathVariable String id) {
        return Result.success(roleService.getRolePermissionIds(id));
    }
}
