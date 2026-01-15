package com.kh.boot.security.domain;

/**
 * Base authentication interface for all user types.
 */
public interface BaseAuthentication {

    /**
     * Get user ID
     */
    String getId();

    /**
     * Get username
     */
    String getUsername();

    /**
     * Get password
     */
    String getPassword();

    /**
     * Get user status (1: enabled, 0: disabled)
     */
    Integer getStatus();

    /**
     * Get user type (admin, member, etc.)
     */
    String getUserType();
}
