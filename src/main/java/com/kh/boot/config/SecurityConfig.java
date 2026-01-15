package com.kh.boot.config;

import org.springframework.beans.factory.annotation.Value;
import com.kh.boot.security.filter.JwtAuthenticationTokenFilter;
import com.kh.boot.security.handler.RestAccessDeniedHandler;
import com.kh.boot.security.handler.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Autowired
    private com.kh.boot.service.SmsService smsService;

    @Autowired
    private com.kh.boot.service.UserService userService;

    @Autowired
    private com.kh.boot.security.service.UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private com.kh.boot.util.JwtUtil jwtUtil;

    @Autowired
    private com.kh.boot.cache.AuthCache authCache;

    @Bean
    public com.kh.boot.security.sms.SmsAuthenticationProvider smsAuthenticationProvider() {
        com.kh.boot.security.sms.SmsAuthenticationProvider provider = new com.kh.boot.security.sms.SmsAuthenticationProvider();
        provider.setSmsService(smsService);
        provider.setUserDetailsService((com.kh.boot.security.sms.SmsUserDetailsService) userService);
        return provider;
    }

    @Value("${kh.security.rsa.private-key}")
    private String privateKey;

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityMatcher("/**") // Explicitly state this is the default/fallback
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/admin/auth/login", "/admin/auth/register",
                                "/admin/auth/sms/code", "/admin/auth/login/sms")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/doc.html",
                                "/webjars/**", "/error") // Permit /error
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler));

        http.authenticationProvider(smsAuthenticationProvider());
        http.authenticationProvider(daoAuthenticationProvider());

        // SMS Filter
        com.kh.boot.security.sms.SmsAuthenticationFilter smsFilter = new com.kh.boot.security.sms.SmsAuthenticationFilter(
                "/admin/auth/login/sms");
        smsFilter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        smsFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        smsFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        http.addFilterBefore(smsFilter, UsernamePasswordAuthenticationFilter.class);

        // JSON Username/Password Filter
        com.kh.boot.security.filter.JsonUsernamePasswordAuthenticationFilter jsonFilter = new com.kh.boot.security.filter.JsonUsernamePasswordAuthenticationFilter(
                "/admin/auth/login");
        jsonFilter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        jsonFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        jsonFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        jsonFilter.setPrivateKey(privateKey);
        http.addFilterBefore(jsonFilter, UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider daoAuthenticationProvider() {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider provider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    private org.springframework.security.web.authentication.AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.setContentType("application/json;charset=UTF-8");
            com.kh.boot.security.domain.LoginUser loginUser = (com.kh.boot.security.domain.LoginUser) authentication
                    .getPrincipal();
            String username = loginUser.getUsername();
            String userType = loginUser.getUserType(); // admin or member

            // Generate Token
            String token = jwtUtil.generateToken(username, userType);

            // Cache Token
            authCache.putToken(username, userType, token);
            authCache.putUser(username, userType, loginUser);

            // Track Online
            com.kh.boot.dto.KhOnlineUserDTO onlineUser = new com.kh.boot.dto.KhOnlineUserDTO();
            onlineUser.setUsername(username);
            onlineUser.setUserType(userType);
            onlineUser.setToken(token);
            authCache.putOnlineUser(onlineUser);

            response.getWriter().write("{\"code\":200,\"msg\":\"Login success\",\"data\":\"" + token + "\"}");
        };
    }

    private org.springframework.security.web.authentication.AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"" + exception.getMessage() + "\",\"data\":null}");
        };
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
