package com.kh.boot.cache;

import com.kh.boot.dto.KhOnlineUserDTO;
import com.kh.boot.security.domain.LoginUser;
import java.util.List;

/**
 * Security Cache interface for managing authentication and identity data.
 * Enhanced to handle multiple user types (admin, member, etc.)
 */
public interface AuthCache {

    /**
     * Cache user information
     */
    void putUser(String username, String userType, LoginUser user);

    /**
     * Get user information from cache
     */
    LoginUser getUser(String username, String userType);

    /**
     * Cache authentication token
     */
    void putToken(String username, String userType, String token);

    /**
     * Get authentication token from cache
     */
    String getToken(String username, String userType);

    /**
     * Remove all data associated with a user
     */
    void remove(String username, String userType);

    /**
     * Check if user is in cache
     */
    boolean containsUser(String username, String userType);

    /**
     * Get all online users (all types)
     */
    List<KhOnlineUserDTO> listOnlineUsers();

    /**
     * Cache online user details
     */
    void putOnlineUser(KhOnlineUserDTO onlineUser);

    /**
     * Get online users with pagination
     */
    com.kh.boot.common.PageData<KhOnlineUserDTO> pageOnlineUsers(int current, int size);

    /**
     * Cache user menus
     */
    void putMenus(String userId, java.util.List<com.kh.boot.vo.KhRouterVo> menus);

    /**
     * Get user menus from cache
     */
    java.util.List<com.kh.boot.vo.KhRouterVo> getMenus(String userId);

    /**
     * Evict specific user's menu cache
     */
    void evictMenus(String userId);

    /**
     * Evict all users' menu cache (when permissions change)
     */
    void evictAllMenus();
}
