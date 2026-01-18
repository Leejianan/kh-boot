package com.kh.boot.security.service;

import com.kh.boot.entity.KhUser;
import com.kh.boot.service.UserService;
import com.kh.boot.security.domain.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        KhUser user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        // Load permissions
        List<String> permissions = userService.getPermissionsByUserId(user.getId());
        permissions.add("ROLE_ADMIN"); // Generic Role Injection

        LoginUser loginUser = new LoginUser(user, permissions);
        loginUser.setUserType("admin");
        return loginUser;
    }
}
