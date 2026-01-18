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
import com.kh.boot.mapper.RoleMapper;
import com.kh.boot.mapper.UserMapper;
import com.kh.boot.mapper.UserRoleMapper;
import com.kh.boot.service.UserService;

import com.kh.boot.vo.KhMetaVo;
import com.kh.boot.vo.KhRouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kh.boot.util.EntityUtils;

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
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private com.kh.boot.cache.AuthCache authCache;

    @org.springframework.beans.factory.annotation.Value("${kh.security.rsa.private-key}")
    private String privateKey;

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
        permissions.add("ROLE_ADMIN"); // Hardcode Generic Permission/Role
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
        permissions.add("ROLE_ADMIN"); // Hardcode Generic Permission/Role
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
            List<String> distinctRoleIds = roleIds.stream().distinct().collect(Collectors.toList());
            for (String roleId : distinctRoleIds) {
                KhUserRole userRole = new KhUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        // Evict this user's menu cache
        authCache.evictMenus(userId);

        // Kick user offline - invalidate their token to force re-login
        KhUser user = getById(userId);
        if (user != null && user.getUsername() != null) {
            authCache.remove(user.getUsername(), com.kh.boot.constant.UserType.ADMIN.getValue());
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

        // Convert and populate roleNames for each user
        return result.convert(user -> {
            KhUserDTO dto = userConverter.toDto(user);
            List<String> roleNames = roleMapper.selectRoleNamesByUserId(user.getId());
            dto.setRoleNames(roleNames != null && !roleNames.isEmpty() ? roleNames : null);
            return dto;
        });
    }

    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        List<KhUserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<KhUserRole>().eq("user_id", userId));
        return userRoles.stream()
                .map(KhUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.kh.boot.entity.KhRole> getRolesByUserId(String userId) {
        List<String> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return roleMapper.selectBatchIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(com.kh.boot.dto.KhUserCreateDTO createDTO) {
        KhUser user = userConverter.toEntity(createDTO);

        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        // Set default status if null (handled by DTO default but good to be safe)
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setAuditStatus(1); // Auto approve for admin created users

        EntityUtils.initInsert(user);

        save(user);

        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), createDTO.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(com.kh.boot.dto.KhUserUpdateDTO updateDTO) {
        KhUser user = userConverter.toEntity(updateDTO);

        // Handle password update only if provided
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        } else {
            user.setPassword(null); // Ensure null password doesn't overwrite existing
        }

        EntityUtils.initUpdate(user);
        updateById(user);

        boolean shouldKick = false;

        // If status is changed to disabled (0), kick user
        if (updateDTO.getStatus() != null && updateDTO.getStatus() == 0) {
            shouldKick = true;
        }

        // Update roles if provided
        if (updateDTO.getRoleIds() != null) {
            assignRoles(user.getId(), updateDTO.getRoleIds());
            // assignRoles already handles kicking, so no need to set shouldKick here
        } else if (shouldKick) {
            // If roles were not updated (which kicks internally), but status was disabled,
            // kick manually
            KhUser currentUser = getById(user.getId());
            if (currentUser != null && currentUser.getUsername() != null) {
                authCache.remove(currentUser.getUsername(), com.kh.boot.constant.UserType.ADMIN.getValue());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        removeById(id);
        // Remove associated roles
        userRoleMapper.delete(new QueryWrapper<KhUserRole>().eq("user_id", id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(com.kh.boot.dto.KhUserResetPasswordDTO resetPasswordDTO) {
        KhUser user = getById(resetPasswordDTO.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        try {
            // 1. Decrypt password (RSA)
            String decryptedPassword = com.kh.boot.util.RsaUtils.decrypt(resetPasswordDTO.getPassword(), privateKey);

            // 2. Encode password (BCrypt)
            user.setPassword(passwordEncoder.encode(decryptedPassword));

            // 3. Update
            updateById(user);
        } catch (Exception e) {
            throw new RuntimeException("密码重置失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userId, com.kh.boot.dto.KhUserProfileDTO profileDTO) {
        KhUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (profileDTO.getRealName() != null)
            user.setRealName(profileDTO.getRealName());
        if (profileDTO.getGender() != null)
            user.setGender(profileDTO.getGender());
        if (profileDTO.getPhone() != null)
            user.setPhone(profileDTO.getPhone());
        if (profileDTO.getEmail() != null)
            user.setEmail(profileDTO.getEmail());
        if (profileDTO.getAvatar() != null)
            user.setAvatar(profileDTO.getAvatar());

        com.kh.boot.util.EntityUtils.initUpdate(user);
        updateById(user);

        // Update Cache
        try {
            LoginUser loginUser = com.kh.boot.util.SecurityUtils.getLoginUser();
            if (loginUser != null) {
                // Determine userType string (assuming "admin" for now if null, or use existing)
                // Note: userType in LoginUser is a String, e.g. "admin" or "1" depending on
                // login
                // logic.
                // We just update the fields.
                if (profileDTO.getRealName() != null)
                    loginUser.setUsername(user.getUsername()); // Username usually doesn't change, but RealName isn't in
                                                               // LoginUser directly unless we cast

                // LoginUser has specific fields for profile
                if (profileDTO.getAvatar() != null)
                    loginUser.setAvatar(profileDTO.getAvatar());
                if (profileDTO.getPhone() != null)
                    loginUser.setPhone(profileDTO.getPhone());
                if (profileDTO.getEmail() != null)
                    loginUser.setEmail(profileDTO.getEmail());

                // RealName is not in LoginUser top-level fields in the version I saw?
                // Let's check LoginUser definition again.
                // It has: email, phone, avatar.
                // RealName is NOT in LoginUser. So we don't need to update it in cache unless
                // we care about KhUser inside it.
                // LoginUser constructor wraps KhUser.
                // But LoginUser properties like email/phone/avatar are flat.
                // For realName/Gender, they might be accessed via KhUser object if it's stored?
                // LoginUser extends UserDetails, it doesn't seem to hold a ref to KhUser after
                // construction?
                // Constructor:
                // if (user instanceof KhUser khUser) { ... this.avatar = khUser.getAvatar()...
                // }
                // So flat fields are used.
                // If realName is needed, we might need to update if it was there. But it's not.
                // So just updating avatar, phone, email is enough for now.

                String userType = loginUser.getUserType();
                // If userType is null, we might have issue. But it should come from token
                // extraction or login.
                if (userType == null)
                    userType = "admin"; // Fallback

                authCache.putUser(loginUser.getUsername(), userType, loginUser);
            }
        } catch (Exception e) {
            log.error("Failed to update user cache", e);
            // Don't fail the transaction just because cache update failed, or do we?
            // Usually we log it.
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, String oldPassword, String newPassword) {
        KhUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        EntityUtils.initUpdate(user);
        updateById(user);
    }
}
