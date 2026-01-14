package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.converter.RoleConverter;
import com.kh.boot.dto.KhRoleDTO;
import com.kh.boot.entity.KhRole;
import com.kh.boot.entity.KhRolePermission;
import com.kh.boot.mapper.RoleMapper;
import com.kh.boot.mapper.RolePermissionMapper;
import com.kh.boot.service.RoleService;
import com.kh.boot.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, KhRole> implements RoleService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private RoleConverter roleConverter;

    @Override
    public IPage<KhRoleDTO> getRolePage(Page<KhRole> page, String name) {
        QueryWrapper<KhRole> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        wrapper.orderByAsc("sort");
        IPage<KhRole> rolePage = baseMapper.selectPage(page, wrapper);
        return rolePage.convert(roleConverter::toDto);
    }

    @Override
    public void addRole(KhRoleDTO roleDTO) {
        KhRole role = roleConverter.toEntity(roleDTO);
        EntityUtils.initInsert(role);
        baseMapper.insert(role);
    }

    @Override
    public void updateRole(KhRoleDTO roleDTO) {
        KhRole role = roleConverter.toEntity(roleDTO);
        EntityUtils.initUpdate(role);
        baseMapper.updateById(role);
    }

    @Override
    public void deleteRole(String id) {
        baseMapper.deleteById(id);
        QueryWrapper<KhRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", id);
        rolePermissionMapper.delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(String roleId, List<String> permissionIds) {
        QueryWrapper<KhRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        rolePermissionMapper.delete(queryWrapper);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (String permId : permissionIds) {
                KhRolePermission rp = new KhRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permId);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<String> getRolePermissionIds(String roleId) {
        QueryWrapper<KhRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        List<KhRolePermission> list = rolePermissionMapper.selectList(queryWrapper);
        return list.stream().map(KhRolePermission::getPermissionId).collect(Collectors.toList());
    }
}
