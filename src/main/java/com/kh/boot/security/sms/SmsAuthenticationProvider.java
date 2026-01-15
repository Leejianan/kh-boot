package com.kh.boot.security.sms;

import com.kh.boot.service.SmsService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;

public class SmsAuthenticationProvider implements AuthenticationProvider {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private SmsUserDetailsService userDetailsService;
    private SmsService smsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken authenticationToken = (SmsAuthenticationToken) authentication;

        String mobile = (String) authenticationToken.getPrincipal();
        String code = (String) authenticationToken.getCredentials();

        // 1. Verify SMS Code
        if (!smsService.verifyCode(mobile, code)) {
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "用户名或密码错误"));
        }

        // 2. Load User
        UserDetails user = userDetailsService.loadUserByPhone(mobile);
        if (user == null) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }

        // 3. Check Account Status
        if (!user.isAccountNonLocked()) {
            throw new InternalAuthenticationServiceException("账户已被锁定");
        }
        if (!user.isEnabled()) {
            throw new InternalAuthenticationServiceException("账户已禁用");
        }
        if (!user.isAccountNonExpired()) {
            throw new InternalAuthenticationServiceException("账户已过期");
        }

        // 4. Create Authenticated Token
        SmsAuthenticationToken authenticationResult = new SmsAuthenticationToken(user, user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());

        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (SmsAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setUserDetailsService(SmsUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }
}
