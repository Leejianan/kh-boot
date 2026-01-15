package com.kh.boot.config;

import com.kh.boot.cache.AuthCache;
import com.kh.boot.dto.KhOnlineUserDTO;
import com.kh.boot.security.domain.LoginUser;
import com.kh.boot.security.email.EmailAuthenticationFilter;
import com.kh.boot.security.email.EmailAuthenticationProvider;
import com.kh.boot.security.email.EmailUserDetailsService;
import com.kh.boot.security.filter.JsonUsernamePasswordAuthenticationFilter;
import com.kh.boot.security.filter.JwtAuthenticationTokenFilter;
import com.kh.boot.security.handler.RestAccessDeniedHandler;
import com.kh.boot.security.handler.RestAuthenticationEntryPoint;
import com.kh.boot.security.service.UserDetailsServiceImpl;
import com.kh.boot.security.sms.SmsAuthenticationFilter;
import com.kh.boot.security.sms.SmsAuthenticationProvider;
import com.kh.boot.security.sms.SmsUserDetailsService;
import com.kh.boot.service.EmailService;
import com.kh.boot.service.SmsService;
import com.kh.boot.service.UserService;
import com.kh.boot.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthCache authCache;

    @Value("${kh.security.rsa.private-key}")
    private String privateKey;

    @Value("${kh.security.cors.enabled:true}")
    private boolean corsEnabled;

    @Value("${kh.security.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable());

        if (corsEnabled) {
            http.cors(Customizer.withDefaults());
        } else {
            http.cors(cors -> cors.disable());
        }

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityMatcher("/**") // Explicitly state this is the default/fallback
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/admin/auth/login", "/admin/auth/register",
                                "/admin/auth/sms/code", "/admin/auth/login/sms", "/admin/auth/email/code",
                                "/admin/auth/login/email")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/doc.html",
                                "/webjars/**", "/error", "/")
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler));

        // Providers are auto-configured because they are @Beans. No need to add them
        // manually to http.

        // SMS Filter
        SmsAuthenticationFilter smsFilter = smsAuthenticationFilter(authenticationConfiguration);
        http.addFilterBefore(smsFilter, UsernamePasswordAuthenticationFilter.class);

        // Email Filter
        EmailAuthenticationFilter emailFilter = emailAuthenticationFilter(authenticationConfiguration);
        http.addFilterBefore(emailFilter, UsernamePasswordAuthenticationFilter.class);

        // JSON Username/Password Filter
        JsonUsernamePasswordAuthenticationFilter jsonFilter = jsonUsernamePasswordAuthenticationFilter(
                authenticationConfiguration);
        http.addFilterBefore(jsonFilter, UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SmsAuthenticationProvider smsAuthenticationProvider() {
        SmsAuthenticationProvider provider = new SmsAuthenticationProvider();
        provider.setSmsService(smsService);
        provider.setUserDetailsService((SmsUserDetailsService) userService);
        return provider;
    }

    @Bean
    public EmailAuthenticationProvider emailAuthenticationProvider() {
        EmailAuthenticationProvider provider = new EmailAuthenticationProvider();
        provider.setEmailService(emailService);
        provider.setUserDetailsService((EmailUserDetailsService) userService);
        return provider;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(false); // Make it easier to debug
        return provider;
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.setContentType("application/json;charset=UTF-8");
            LoginUser loginUser = (LoginUser) authentication
                    .getPrincipal();
            String username = loginUser.getUsername();
            String userType = loginUser.getUserType(); // admin or member

            // Generate Token
            String token = jwtUtil.generateToken(username, userType);

            // Cache Token
            authCache.putToken(username, userType, token);
            authCache.putUser(username, userType, loginUser);

            // Track Online
            KhOnlineUserDTO onlineUser = new KhOnlineUserDTO();
            onlineUser.setUsername(username);
            onlineUser.setUserType(userType);
            onlineUser.setToken(token);
            authCache.putOnlineUser(onlineUser);

            response.getWriter().write("{\"code\":200,\"msg\":\"Login success\",\"data\":\"" + token + "\"}");
        };
    }

    private AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"" + exception.getMessage() + "\",\"data\":null}");
        };
    }

    @Bean
    public SmsAuthenticationFilter smsAuthenticationFilter(AuthenticationConfiguration authConfig) throws Exception {
        SmsAuthenticationFilter filter = new SmsAuthenticationFilter("/admin/auth/login/sms");
        filter.setAuthenticationManager(authConfig.getAuthenticationManager());
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }

    @Bean
    public EmailAuthenticationFilter emailAuthenticationFilter(AuthenticationConfiguration authConfig)
            throws Exception {
        EmailAuthenticationFilter filter = new EmailAuthenticationFilter("/admin/auth/login/email");
        filter.setAuthenticationManager(authConfig.getAuthenticationManager());
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }

    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter(
            AuthenticationConfiguration authConfig) throws Exception {
        JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(
                "/admin/auth/login");
        filter.setAuthenticationManager(authConfig.getAuthenticationManager());
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        filter.setPrivateKey(privateKey);
        return filter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern(allowedOrigins);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
