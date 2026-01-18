package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.converter.RoleConverter;
import com.kh.boot.dto.KhRoleDTO;
import com.kh.boot.entity.KhRole;
import com.kh.boot.entity.KhRolePermission;
import com.kh.boot.entity.KhUser;
import com.kh.boot.entity.KhUserRole;
import com.kh.boot.mapper.RoleMapper;
import com.kh.boot.mapper.RolePermissionMapper;
import com.kh.boot.mapper.UserMapper;
import com.kh.boot.mapper.UserRoleMapper;
import com.kh.boot.query.RoleQuery;
import com.kh.boot.service.RoleService;
import com.kh.boot.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import com.kh.boot.cache.AuthCache;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, KhRole> implements RoleService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private RoleConverter roleConverter;

    @Autowired
    private AuthCache authCache;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<KhRoleDTO> getRolePage(RoleQuery query) {
        Page<KhRole> pageParam = query.toPage();
        LambdaQueryWrapper<KhRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), KhRole::getName, query.getName());

        // If no custom sorting, set default sort
        if (!StringUtils.hasText(query.getOrderBy())) {
            wrapper.orderByAsc(KhRole::getSort);
        }

        IPage<KhRole> rolePage = baseMapper.selectPage(pageParam, wrapper);
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
        LambdaQueryWrapper<KhRolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KhRolePermission::getRoleId, id);
        rolePermissionMapper.delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(String roleId, List<String> permissionIds) {
        LambdaQueryWrapper<KhRolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KhRolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(queryWrapper);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (String permId : permissionIds) {
                KhRolePermission rp = new KhRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permId);
                rolePermissionMapper.insert(rp);
            }
        }
        // Evict all menu cache since role permissions affect multiple users
        authCache.evictAllMenus();

        // Kick all users with this role offline
        LambdaQueryWrapper<KhUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(KhUserRole::getRoleId, roleId);
        List<KhUserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        for (KhUserRole ur : userRoles) {
            KhUser user = userMapper.selectById(ur.getUserId());
            if (user != null && user.getUsername() != null) {
                authCache.remove(user.getUsername(), com.kh.boot.constant.UserType.ADMIN.getValue());
            }
        }
    }

    @Override
    public List<String> getRolePermissionIds(String roleId) {
        LambdaQueryWrapper<KhRolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KhRolePermission::getRoleId, roleId);
        List<KhRolePermission> list = rolePermissionMapper.selectList(queryWrapper);
        return list.stream().map(KhRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    public List<KhRoleDTO> listAll() {
        return roleConverter.toDtoList(list());
    }
}
