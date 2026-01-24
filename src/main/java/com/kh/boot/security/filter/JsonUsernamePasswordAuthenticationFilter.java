package com.kh.boot.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

public class JsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_FILTER_PROCESSES_URL = "/auth/login";
    private static final String HTTP_METHOD = "POST";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher(DEFAULT_FILTER_PROCESSES_URL, HTTP_METHOD));
    }

    public JsonUsernamePasswordAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(new AntPathRequestMatcher(defaultFilterProcessesUrl, HTTP_METHOD));
    }

    private String privateKey;
    private boolean allowPlaintextPassword = true;

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setAllowPlaintextPassword(boolean allowPlaintextPassword) {
        this.allowPlaintextPassword = allowPlaintextPassword;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        if (!request.getMethod().equals(HTTP_METHOD)) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        if (request.getContentType().equals("application/json")
                || request.getContentType().startsWith("application/json")) {
            Map<String, String> loginData = objectMapper.readValue(request.getInputStream(),
                    new TypeReference<Map<String, String>>() {
                    });
            String username = loginData.get("username");
            String password = loginData.get("password");

            if (username == null) {
                username = "";
            }
            if (password == null) {
                password = "";
            }

            // Decrypt password if private key is configured
            if (privateKey != null && !privateKey.isEmpty()) {
                try {
                    // Assuming RsaUtils is available in com.kh.boot.util
                    password = com.kh.boot.util.RsaUtils.decrypt(password, privateKey);
                } catch (Exception e) {
                    if (!allowPlaintextPassword) {
                        throw new AuthenticationServiceException(
                                "Plaintext password login is disallowed in this environment.");
                    }
                    // Ignore decryption error, use as is (might be plaintext or invalid)
                    // throw new AuthenticationServiceException("Invalid encrypted password");
                }
            } else if (!allowPlaintextPassword) {
                // If no key is configured BUT plaintext is disallowed, this is an invalid
                // configuration for this environment
                throw new AuthenticationServiceException("Authentication configuration error: Cryptography required.");
            }

            username = username.trim();

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username,
                    password);
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } else {
            // Fallback to standard form if not JSON (Optional, but good for robust
            // handling)
            throw new AuthenticationServiceException(
                    "Authentication content-type not supported: " + request.getContentType());
        }
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
