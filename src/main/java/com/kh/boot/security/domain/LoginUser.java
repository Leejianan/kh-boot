package com.kh.boot.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kh.boot.entity.KhUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Logged-in User Object
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    private String userId;

    /**
     * Username
     */
    private String username;

    /**
     * Password (Ignored in Serialization)
     */
    @JsonIgnore
    private String password;

    /**
     * User Code
     */
    private String userCode;

    /**
     * Email
     */
    private String email;

    /**
     * Phone
     */
    private String phone;

    /**
     * Status
     */
    private Integer status;

    /**
     * Login Time
     */
    private Date loginTime;

    /**
     * Expiration Time
     */
    private Long expireTime;

    /**
     * Login IP
     */
    private String ipaddr;

    /**
     * Login Location
     */
    private String loginLocation;

    /**
     * Browser Type
     */
    private String browser;

    /**
     * Operating System
     */
    private String os;

    /**
     * Avatar URL
     */
    private String avatar;

    /**
     * User Type (admin/member)
     */
    /**
     * User Type (admin/member)
     */
    private String userType;

    /**
     * Wechat OpenID
     */
    private String openId;

    /**
     * Permissions List
     */
    private List<String> permissions;

    public LoginUser(BaseAuthentication user, List<String> permissions) {
        if (user != null) {
            this.userId = user.getId();
            this.username = user.getUsername();
            this.password = user.getPassword();
            this.userType = user.getUserType();
            this.status = user.getStatus();
            // Extend with more fields if BaseAuthentication is expanded or use casting if
            // needed for specific fields
            if (user instanceof KhUser khUser) {
                this.userCode = khUser.getUserCode();
                this.email = khUser.getEmail();
                this.phone = khUser.getPhone();
                this.avatar = khUser.getAvatar();
            }
        }
        this.permissions = permissions;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null) {
            return List.of();
        }
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
