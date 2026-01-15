package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.kh.boot.security.domain.LoginUser;

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
        return getOne(new LambdaQueryWrapper<KhUser>()
                .eq(KhUser::getUsername, username));
    }

    @Override
    public KhUser findByPhone(String phone) {
        return getOne(new LambdaQueryWrapper<KhUser>()
                .eq(KhUser::getPhone, phone));
    }

    @Override
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        KhUser user = findByPhone(phone);
        if (user == null) {
            throw new UsernameNotFoundException("手机号未注册: " + phone);
        }

        List<String> permissions = this.getPermissionsByUserId(user.getId());
        LoginUser loginUser = new LoginUser(user, permissions);
        loginUser.setUserType(com.kh.boot.constant.UserType.ADMIN.getValue());

        return loginUser;
    }

    @Override
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        KhUser user = getOne(new LambdaQueryWrapper<KhUser>()
                .eq(KhUser::getEmail, email));
        if (user == null) {
            throw new UsernameNotFoundException("邮箱未注册: " + email);
        }

        List<String> permissions = this.getPermissionsByUserId(user.getId());
        LoginUser loginUser = new LoginUser(user, permissions);
        loginUser.setUserType(com.kh.boot.constant.UserType.ADMIN.getValue());

        return loginUser;
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
    public com.baomidou.mybatisplus.core.metadata.IPage<KhUserDTO> page(com.kh.boot.query.UserQuery query) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<KhUser> pageParam = query.toPage();

        LambdaQueryWrapper<KhUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(org.springframework.util.StringUtils.hasText(query.getUsername()), KhUser::getUsername,
                query.getUsername());
        wrapper.like(org.springframework.util.StringUtils.hasText(query.getPhone()), KhUser::getPhone,
                query.getPhone());
        wrapper.like(org.springframework.util.StringUtils.hasText(query.getEmail()), KhUser::getEmail,
                query.getEmail());
        wrapper.eq(query.getStatus() != null, KhUser::getStatus, query.getStatus());

        // If no custom sorting, set default sort
        if (!org.springframework.util.StringUtils.hasText(query.getOrderBy())) {
            wrapper.orderByDesc(KhUser::getCreateTime);
        }

        com.baomidou.mybatisplus.core.metadata.IPage<KhUser> result = baseMapper.selectPage(pageParam, wrapper);
        return result.convert(userConverter::toDto);
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
