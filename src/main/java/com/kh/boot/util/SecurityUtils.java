package com.kh.boot.util;

import com.kh.boot.security.domain.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security Utilities
 */
public class SecurityUtils {

    /**
     * Get current authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get current login user
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get current user ID
     */
    public static String getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * Get current username
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : "system";
    }

    /**
     * Get current user name (real name)
     */
    public static String getRealName() {
        // User entity doesn't have a 'nickName' or 'realName' yet,
        // fallback to username for now.
        return getUsername();
    }
}
