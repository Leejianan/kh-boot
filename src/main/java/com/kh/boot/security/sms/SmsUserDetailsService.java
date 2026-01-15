package com.kh.boot.security.sms;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Strategy interface for loading user details by phone number.
 */
public interface SmsUserDetailsService {

    /**
     * Load user by phone number.
     * 
     * @param phone the phone number identifying the user
     * @return a fully populated user record (never null)
     * @throws UsernameNotFoundException if the user could not be found
     */
    UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException;
}
