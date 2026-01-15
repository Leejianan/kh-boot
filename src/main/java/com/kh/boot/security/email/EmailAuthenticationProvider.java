package com.kh.boot.security.email;

import com.kh.boot.service.EmailService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;

public class EmailAuthenticationProvider implements AuthenticationProvider {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private EmailUserDetailsService userDetailsService;
    private EmailService emailService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        EmailAuthenticationToken authenticationToken = (EmailAuthenticationToken) authentication;

        String email = (String) authenticationToken.getPrincipal();
        String code = (String) authenticationToken.getCredentials();

        // 1. Verify Email Code
        if (!emailService.verifyCode(email, code)) {
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "用户名或密码错误"));
        }

        // 2. Load User
        UserDetails user = userDetailsService.loadUserByEmail(email);
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
        EmailAuthenticationToken authenticationResult = new EmailAuthenticationToken(user, user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());

        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (EmailAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setUserDetailsService(EmailUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}
