package com.kh.boot.constant;

/**
 * User Types
 */
public enum UserType {
    ADMIN("admin"),
    MEMBER("member");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
