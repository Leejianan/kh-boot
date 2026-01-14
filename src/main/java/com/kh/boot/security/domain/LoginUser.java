package com.kh.boot.security.domain;

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
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * User Entity
     */
    private KhUser user;

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
     * User Type (admin/member)
     */
    private String userType;

    /**
     * Permissions List
     */
    private List<String> permissions;

    public LoginUser(KhUser user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null) {
            return List.of();
        }
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return user != null ? user.getUsername() : null;
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
        return user != null && user.getStatus() != null && user.getStatus() == 1;
    }
}
