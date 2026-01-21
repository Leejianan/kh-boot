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
import org.springframework.context.ApplicationEventPublisher;

/**
 * Redis-based implementation of AuthCache.
 * Provides distributed session and token management.
 */
@Component("redisAuthCache")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisAuthCache implements AuthCache {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${kh.cache.timeout:120}")
    private Integer timeout;

    private static final String KEY_USER = "auth:user:";
    private static final String KEY_TOKEN = "auth:token:";
    private static final String KEY_ONLINE = "auth:online:";
    private static final String KEY_ONLINE_IDS = "auth:online:ids";

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
        // Remove from ZSet
        redisTemplate.opsForZSet().remove(KEY_ONLINE_IDS, userType + ":" + username);
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

        // Check for existing session (duplicate login)
        Object existingValue = redisTemplate.opsForValue().get(key);
        if (existingValue instanceof KhOnlineUserDTO) {
            KhOnlineUserDTO oldUser = (KhOnlineUserDTO) existingValue;
            if (!oldUser.getToken().equals(onlineUser.getToken())) {
                eventPublisher.publishEvent(new com.kh.boot.event.KickOutEvent(this,
                        oldUser.getUsername(), oldUser.getUserType(), oldUser.getToken(),
                        "您的账号在另一地点登录，密码可能已泄露！如非本人操作，请立即修改密码。"));
            }
        }

        redisTemplate.opsForValue().set(key, onlineUser, timeout, TimeUnit.MINUTES);
        // Add to ZSet with current timestamp as score
        redisTemplate.opsForZSet().add(KEY_ONLINE_IDS, onlineUser.getUserType() + ":" + onlineUser.getUsername(),
                System.currentTimeMillis());
    }

    @Override
    public com.kh.boot.common.PageData<KhOnlineUserDTO> pageOnlineUsers(int current, int size) {
        long total = redisTemplate.opsForZSet().zCard(KEY_ONLINE_IDS);
        if (total == 0) {
            return new com.kh.boot.common.PageData<>(0L, (long) current, (long) size, new ArrayList<>());
        }

        long start = (long) (current - 1) * size;
        long end = start + size - 1;

        // ZREVRANGE for descending order (newest first)
        Set<Object> members = redisTemplate.opsForZSet().reverseRange(KEY_ONLINE_IDS, start, end);
        if (members == null || members.isEmpty()) {
            return new com.kh.boot.common.PageData<>(total, (long) current, (long) size, new ArrayList<>());
        }

        List<String> keys = members.stream()
                .map(obj -> {
                    String member = (String) obj;
                    // member format: userType:username
                    // key format: auth:online:userType:username
                    // Need to split to reconstruct key correctly using logic of getTypedKey logic?
                    // Actually getTypedKey("auth:online:", username, userType) -> "auth:online:" +
                    // userType + ":" + username
                    // So just prepending "auth:online:" to member string works if member is
                    // userType:username
                    return KEY_ONLINE + member;
                })
                .collect(Collectors.toList());

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        List<KhOnlineUserDTO> records = new ArrayList<>();
        if (values != null) {
            for (Object v : values) {
                if (v instanceof KhOnlineUserDTO) {
                    records.add((KhOnlineUserDTO) v);
                }
            }
        }

        return new com.kh.boot.common.PageData<>(total, (long) current, (long) size, records);
    }

    private static final String KEY_MENUS = "auth:menus:";

    @Override
    public void putMenus(String userId, List<com.kh.boot.vo.KhRouterVo> menus) {
        String key = KEY_MENUS + userId;
        redisTemplate.opsForValue().set(key, menus, timeout, TimeUnit.MINUTES);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<com.kh.boot.vo.KhRouterVo> getMenus(String userId) {
        String key = KEY_MENUS + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof List) {
            return (List<com.kh.boot.vo.KhRouterVo>) value;
        }
        return null;
    }

    @Override
    public void evictMenus(String userId) {
        redisTemplate.delete(KEY_MENUS + userId);
    }

    @Override
    public void evictAllMenus() {
        Set<String> keys = redisTemplate.keys(KEY_MENUS + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
