package com.kh.boot.event;

import org.springframework.context.ApplicationEvent;

public class KickOutEvent extends ApplicationEvent {
    private final String username;
    private final String userType;
    private final String reason;
    private final String token;

    public KickOutEvent(Object source, String username, String userType, String token, String reason) {
        super(source);
        this.username = username;
        this.userType = userType;
        this.token = token;
        this.reason = reason;
    }

    public String getUsername() {
        return username;
    }

    public String getUserType() {
        return userType;
    }

    public String getReason() {
        return reason;
    }

    public String getToken() {
        return token;
    }
}
