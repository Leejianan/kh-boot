package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.converter.UserConverter;
import com.kh.boot.dto.KhUserDTO;
import com.kh.boot.entity.KhPermission;
import com.kh.boot.entity.KhUser;
import com.kh.boot.entity.KhUserRole;
import com.kh.boot.mapper.PermissionMapper;
import com.kh.boot.mapper.UserMapper;
import com.kh.boot.mapper.UserRoleMapper;
import com.kh.boot.service.UserService;
import com.kh.boot.vo.KhMetaVo;
import com.kh.boot.vo.KhRouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, KhUser> implements UserService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserConverter userConverter;

    @Override
    public KhUser findByUsername(String username) {
        return baseMapper.selectOne(new QueryWrapper<KhUser>().eq("username", username));
    }

    @Override
    public List<KhUser> getUserList() {
        return baseMapper.selectUserList();
    }

    @Override
    public List<KhUserDTO> getUserListDTO() {
        List<KhUser> users = baseMapper.selectUserList();
        return userConverter.toDtoList(users);
    }

    @Override
    public List<String> getPermissionsByUserId(String userId) {
        List<KhPermission> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream()
                .map(KhPermission::getPermissionKey)
                .filter(key -> key != null && !key.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<KhRouterVo> getMenusByUserId(String userId) {
        List<KhPermission> rawMenus = permissionMapper.selectMenuByUserId(userId);
        return buildMenuTree(rawMenus, "0");
    }

    private List<KhRouterVo> buildMenuTree(List<KhPermission> menus, String parentId) {
        List<KhRouterVo> tree = new ArrayList<>();
        for (KhPermission p : menus) {
            if (parentId.equals(p.getParentId())) {
                KhRouterVo node = new KhRouterVo();
                node.setName(p.getName());
                node.setPath(p.getPath());
                node.setComponent(p.getComponent());
                KhMetaVo meta = new KhMetaVo(p.getName(), p.getIcon());
                node.setMeta(meta);
                node.setChildren(buildMenuTree(menus, p.getId()));
                tree.add(node);
            }
        }
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(String userId, List<String> roleIds) {
        userRoleMapper.delete(new QueryWrapper<KhUserRole>().eq("user_id", userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                KhUserRole userRole = new KhUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        List<KhUserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<KhUserRole>().eq("user_id", userId));
        return userRoles.stream()
                .map(KhUserRole::getRoleId)
                .collect(Collectors.toList());
    }
}
