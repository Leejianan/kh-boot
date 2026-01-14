package com.kh.boot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.dto.KhRoleDTO;
import com.kh.boot.entity.KhRole;

import java.util.List;

public interface RoleService extends IService<KhRole> {

    IPage<KhRoleDTO> getRolePage(Page<KhRole> page, String name);

    void addRole(KhRoleDTO roleDTO);

    void updateRole(KhRoleDTO roleDTO);

    void deleteRole(String id);

    void assignPermissions(String roleId, List<String> permissionIds);

    List<String> getRolePermissionIds(String roleId);
}
