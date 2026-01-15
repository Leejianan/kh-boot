package com.kh.boot.security.email;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Strategy interface for loading user details by email.
 */
public interface EmailUserDetailsService {

    /**
     * Load user by email.
     * 
     * @param email the email identifying the user
     * @return a fully populated user record (never null)
     * @throws UsernameNotFoundException if the user could not be found
     */
    UserDetails loadUserByEmail(String email) throws UsernameNotFoundException;
}
