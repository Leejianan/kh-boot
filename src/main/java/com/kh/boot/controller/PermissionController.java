package com.kh.boot.controller;

import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.converter.PermissionConverter;
import com.kh.boot.dto.KhPermissionDTO;
import com.kh.boot.entity.KhPermission;
import com.kh.boot.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Permission Management")
@RestController
@RequestMapping("/permissions")
public class PermissionController extends BaseController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionConverter permissionConverter;

    @Operation(summary = "Get Permission Tree")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public Result<List<KhPermissionDTO>> tree() {
        List<KhPermission> tree = permissionService.getPermissionTree();
        return success(permissionConverter.toDtoList(tree));
    }

    @Operation(summary = "Add Permission")
    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:add')")
    public Result<Boolean> add(@RequestBody KhPermissionDTO dto) {
        KhPermission permission = permissionConverter.toEntity(dto);
        return success(permissionService.save(permission));
    }

    @Operation(summary = "Update Permission")
    @PutMapping
    @PreAuthorize("hasAuthority('system:permission:edit')")
    public Result<Boolean> update(@RequestBody KhPermissionDTO dto) {
        KhPermission permission = permissionConverter.toEntity(dto);
        return success(permissionService.updateById(permission));
    }

    @Operation(summary = "Delete Permission")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    public Result<Boolean> delete(@PathVariable String id) {
        return success(permissionService.removeById(id));
    }
}
