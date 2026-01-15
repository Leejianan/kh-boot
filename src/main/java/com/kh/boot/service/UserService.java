package com.kh.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.dto.KhUserDTO;
import com.kh.boot.entity.KhUser;
import com.kh.boot.vo.KhRouterVo;

import java.util.List;

public interface UserService extends IService<KhUser> {

    KhUser findByUsername(String username);

    KhUser findByPhone(String phone);

    List<KhUser> getUserList();

    List<KhUserDTO> getUserListDTO();

    /**
     * Get permission list by user ID
     * 
     * @param userId User ID
     * @return List of permission keys (e.g. "system:user:add")
     */
    List<String> getPermissionsByUserId(String userId);

    /**
     * Get menu tree by user ID
     */
    List<KhRouterVo> getMenusByUserId(String userId);

    /**
     * Assign roles to user
     */
    void assignRoles(String userId, List<String> roleIds);

    /**
     * Get role IDs by user ID
     */
    List<String> getRoleIdsByUserId(String userId);
}
