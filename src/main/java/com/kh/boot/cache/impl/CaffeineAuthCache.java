package com.kh.boot.cache.impl;

import com.kh.boot.cache.AuthCache;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.dto.KhOnlineUserDTO;
import com.kh.boot.vo.KhRouterVo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Caffeine-based implementation of AuthCache.
 * Uses typed keys (type:username) to support multiple user end-points.
 */
@Component
@ConditionalOnMissingBean(name = "redisAuthCache")
public class CaffeineAuthCache implements AuthCache {

    @Value("${kh.cache.timeout:120}")
    private Integer timeout;

    private Cache<String, LoginUser> userCache;
    private Cache<String, String> tokenCache;
    private Cache<String, KhOnlineUserDTO> onlineUserCache;
    private Cache<String, List<KhRouterVo>> menuCache;

    @PostConstruct
    public void init() {
        userCache = Caffeine.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        tokenCache = Caffeine.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        onlineUserCache = Caffeine.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        menuCache = Caffeine.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }

    private String getTypedKey(String username, String userType) {
        return userType + ":" + username;
    }

    @Override
    public void putUser(String username, String userType, LoginUser user) {
        userCache.put(getTypedKey(username, userType), user);
    }

    @Override
    public LoginUser getUser(String username, String userType) {
        return userCache.getIfPresent(getTypedKey(username, userType));
    }

    @Override
    public void putToken(String username, String userType, String token) {
        tokenCache.put(getTypedKey(username, userType), token);
    }

    @Override
    public String getToken(String username, String userType) {
        return tokenCache.getIfPresent(getTypedKey(username, userType));
    }

    @Override
    public void remove(String username, String userType) {
        String key = getTypedKey(username, userType);
        userCache.invalidate(key);
        tokenCache.invalidate(key);
        onlineUserCache.invalidate(key);
    }

    @Override
    public boolean containsUser(String username, String userType) {
        return userCache.getIfPresent(getTypedKey(username, userType)) != null;
    }

    @Override
    public List<KhOnlineUserDTO> listOnlineUsers() {
        return new ArrayList<>(onlineUserCache.asMap().values());
    }

    @Override
    public void putOnlineUser(KhOnlineUserDTO onlineUser) {
        onlineUserCache.put(getTypedKey(onlineUser.getUsername(), onlineUser.getUserType()), onlineUser);
    }

    @Override
    public com.kh.boot.common.PageData<KhOnlineUserDTO> pageOnlineUsers(int current, int size) {
        List<KhOnlineUserDTO> allUsers = new ArrayList<>(onlineUserCache.asMap().values());

        int total = allUsers.size();
        int fromIndex = (current - 1) * size;
        if (fromIndex >= total) {
            return new com.kh.boot.common.PageData<>((long) total, (long) current, (long) size, new ArrayList<>());
        }
        int toIndex = Math.min(fromIndex + size, total);
        List<KhOnlineUserDTO> pageRecords = allUsers.subList(fromIndex, toIndex);

        return new com.kh.boot.common.PageData<>((long) total, (long) current, (long) size, pageRecords);
    }

    @Override
    public void putMenus(String userId, List<KhRouterVo> menus) {
        menuCache.put(userId, menus);
    }

    @Override
    public List<KhRouterVo> getMenus(String userId) {
        return menuCache.getIfPresent(userId);
    }

    @Override
    public void evictMenus(String userId) {
        menuCache.invalidate(userId);
    }

    @Override
    public void evictAllMenus() {
        menuCache.invalidateAll();
    }
}
