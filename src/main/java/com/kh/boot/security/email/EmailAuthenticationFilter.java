package com.kh.boot.security.email;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import java.io.IOException;

public class EmailAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_EMAIL_KEY = "email";
    public static final String SPRING_SECURITY_FORM_CODE_KEY = "code";

    private String emailParameter = SPRING_SECURITY_FORM_EMAIL_KEY;
    private String codeParameter = SPRING_SECURITY_FORM_CODE_KEY;
    private boolean postOnly = true;

    public EmailAuthenticationFilter() {
        super(new AntPathRequestMatcher("/auth/login/email", "POST"));
    }

    // Constructor to allow custom path
    public EmailAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(new AntPathRequestMatcher(defaultFilterProcessesUrl, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String email = obtainEmail(request);
        String code = obtainCode(request);

        if (email == null) {
            email = "";
        }
        if (code == null) {
            code = "";
        }

        email = email.trim();
        code = code.trim();

        EmailAuthenticationToken authRequest = new EmailAuthenticationToken(email, code);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected String obtainEmail(HttpServletRequest request) {
        return request.getParameter(emailParameter);
    }

    protected String obtainCode(HttpServletRequest request) {
        return request.getParameter(codeParameter);
    }

    protected void setDetails(HttpServletRequest request, EmailAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setEmailParameter(String emailParameter) {
        Assert.hasText(emailParameter, "Email parameter must not be empty or null");
        this.emailParameter = emailParameter;
    }

    public void setCodeParameter(String codeParameter) {
        Assert.hasText(codeParameter, "Code parameter must not be empty or null");
        this.codeParameter = codeParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getEmailParameter() {
        return emailParameter;
    }

    public final String getCodeParameter() {
        return codeParameter;
    }
}
