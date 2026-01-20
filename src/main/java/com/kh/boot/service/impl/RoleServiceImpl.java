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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kh.boot.cache.AuthCache;
import com.kh.boot.constant.RoleCode;
import com.kh.boot.constant.UserType;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, KhRole> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final RoleConverter roleConverter;
    private final AuthCache authCache;
    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final UserService userService;

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
    @Transactional(rollbackFor = Exception.class)
    public void addRole(KhRoleDTO roleDTO) {
        KhRole role = roleConverter.toEntity(roleDTO);
        EntityUtils.initInsert(role);
        baseMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(KhRoleDTO roleDTO) {
        KhRole role = roleConverter.toEntity(roleDTO);
        EntityUtils.initUpdate(role);
        baseMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        baseMapper.deleteById(id);
        LambdaQueryWrapper<KhRolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KhRolePermission::getRoleId, id);
        rolePermissionMapper.delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(String roleId, List<String> permissionIds) {
        // 1. Remove existing permissions
        rolePermissionMapper.delete(new LambdaQueryWrapper<KhRolePermission>()
                .eq(KhRolePermission::getRoleId, roleId));

        // 2. Add new permissions
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<String> distinctPermissionIds = permissionIds.stream()
                    .distinct()
                    .collect(Collectors.toList());

            for (String permissionId : distinctPermissionIds) {
                KhRolePermission rp = new KhRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }

        // 3. Clear auth cache
        // If updating super admin role, only clear menu cache, don't kick users
        KhRole role = getById(roleId);
        boolean isSuperAdmin = role != null && RoleCode.SUPER_ADMIN.equals(role.getRoleKey());

        if (isSuperAdmin) {
            authCache.evictAllMenus();

            // Also update the cached LoginUser permission list so backend @PreAuthorize and
            // /info works immediately
            // Find all users with this role
            List<KhUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<KhUserRole>()
                    .eq(KhUserRole::getRoleId, roleId));

            for (KhUserRole ur : userRoles) {
                String userId = ur.getUserId();
                KhUser user = userMapper.selectById(userId);
                if (user != null) {
                    // Fetch fresh permissions from DB
                    List<String> permissions = userService.getPermissionsByUserId(userId);
                    // Add generic ROLE_ADMIN permission as done in UserServiceImpl.loadUserByXxx
                    permissions.add("ROLE_ADMIN");

                    // Update Redis
                    String userType = UserType.ADMIN.getValue();
                    LoginUser loginUser = authCache.getUser(user.getUsername(), userType);
                    if (loginUser != null) {
                        loginUser.setPermissions(permissions);
                        authCache.putUser(user.getUsername(), userType, loginUser);
                    }
                }
            }
        } else {
            // For normal roles, kick users to force re-login and get new permissions
            List<KhUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<KhUserRole>()
                    .eq(KhUserRole::getRoleId, roleId));

            for (KhUserRole ur : userRoles) {
                String userId = ur.getUserId();
                // Get username needed for cache key
                KhUser user = userMapper.selectById(userId);
                if (user != null && user.getUsername() != null) {
                    authCache.remove(user.getUsername(), UserType.ADMIN.getValue());
                }
            }
        }

        return isSuperAdmin;
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
