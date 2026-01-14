package com.kh.boot.cache.impl;

import com.kh.boot.cache.AuthCache;
import com.kh.boot.dto.KhOnlineUserDTO;
import com.kh.boot.security.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis-based implementation of AuthCache.
 * Provides distributed session and token management.
 */
@Component("redisAuthCache")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisAuthCache implements AuthCache {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${kh.cache.timeout:120}")
    private Integer timeout;

    private static final String KEY_USER = "auth:user:";
    private static final String KEY_TOKEN = "auth:token:";
    private static final String KEY_ONLINE = "auth:online:";

    private String getTypedKey(String prefix, String username, String userType) {
        return prefix + userType + ":" + username;
    }

    @Override
    public void putUser(String username, String userType, LoginUser user) {
        String key = getTypedKey(KEY_USER, username, userType);
        redisTemplate.opsForValue().set(key, user, timeout, TimeUnit.MINUTES);
    }

    @Override
    public LoginUser getUser(String username, String userType) {
        String key = getTypedKey(KEY_USER, username, userType);
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof LoginUser ? (LoginUser) value : null;
    }

    @Override
    public void putToken(String username, String userType, String token) {
        String key = getTypedKey(KEY_TOKEN, username, userType);
        redisTemplate.opsForValue().set(key, token, timeout, TimeUnit.MINUTES);
    }

    @Override
    public String getToken(String username, String userType) {
        String key = getTypedKey(KEY_TOKEN, username, userType);
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof String ? (String) value : null;
    }

    @Override
    public void remove(String username, String userType) {
        redisTemplate.delete(getTypedKey(KEY_USER, username, userType));
        redisTemplate.delete(getTypedKey(KEY_TOKEN, username, userType));
        redisTemplate.delete(getTypedKey(KEY_ONLINE, username, userType));
    }

    @Override
    public boolean containsUser(String username, String userType) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getTypedKey(KEY_USER, username, userType)));
    }

    @Override
    public List<KhOnlineUserDTO> listOnlineUsers() {
        Set<String> keys = redisTemplate.keys(KEY_ONLINE + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream()
                .filter(v -> v instanceof KhOnlineUserDTO)
                .map(v -> (KhOnlineUserDTO) v)
                .collect(Collectors.toList());
    }

    @Override
    public void putOnlineUser(KhOnlineUserDTO onlineUser) {
        String key = getTypedKey(KEY_ONLINE, onlineUser.getUsername(), onlineUser.getUserType());
        redisTemplate.opsForValue().set(key, onlineUser, timeout, TimeUnit.MINUTES);
    }
}
