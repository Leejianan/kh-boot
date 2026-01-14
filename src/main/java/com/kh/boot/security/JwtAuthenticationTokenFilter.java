package com.kh.boot.security;

import com.kh.boot.cache.AuthCache;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthCache authCache;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null) {
            token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // Token invalid
                logger.error("Token invalid or expired: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String userType = jwtUtil.extractUserType(token);
            String cachedToken = authCache.getToken(username, userType);

            if (token != null && token.equals(cachedToken)) {
                // Get pre-loaded LoginUser from cache (contains IP, Browser, etc.)
                LoginUser loginUser = authCache.getUser(username, userType);

                if (loginUser != null && !jwtUtil.isTokenExpired(token)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            loginUser, null, loginUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
