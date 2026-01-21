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

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

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
    private JwtUtil jwtUtil;

    @Autowired
    private AuthCache authCache;

    @Value("${kh.security.rsa.private-key}")
    private String privateKey;

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults());

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityMatcher("/**") // Explicitly state this is the default/fallback
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/admin/auth/login",

                                "/admin/auth/sms/code", "/admin/auth/login/sms", "/admin/auth/email/code",
                                "/admin/auth/login/email")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/doc.html",
                                "/webjars/**", "/error", "/")
                        .permitAll()
                        // WebSocket endpoint - needs to allow SockJS handshake
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler));

        // Providers are auto-configured because they are @Beans. No need to add them
        // manually to http.

        // SMS Filter
        SmsAuthenticationFilter smsFilter = smsAuthenticationFilter(authenticationManager);
        http.addFilterBefore(smsFilter, UsernamePasswordAuthenticationFilter.class);

        // Email Filter
        EmailAuthenticationFilter emailFilter = emailAuthenticationFilter(authenticationManager);
        http.addFilterBefore(emailFilter, UsernamePasswordAuthenticationFilter.class);

        // JSON Username/Password Filter
        JsonUsernamePasswordAuthenticationFilter jsonFilter = jsonUsernamePasswordAuthenticationFilter(
                authenticationManager);
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

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider,
            SmsAuthenticationProvider smsAuthenticationProvider,
            EmailAuthenticationProvider emailAuthenticationProvider) {
        return new ProviderManager(
                Arrays.asList(daoAuthenticationProvider, smsAuthenticationProvider,
                        emailAuthenticationProvider));
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
            onlineUser.setRealName(loginUser.getRealName());
            onlineUser.setToken(token);
            onlineUser.setLoginTime(new java.util.Date());
            onlineUser.setIp(com.kh.boot.util.IpUtils.getIpAddr(request));

            try {
                eu.bitwalker.useragentutils.UserAgent userAgent = eu.bitwalker.useragentutils.UserAgent
                        .parseUserAgentString(request.getHeader("User-Agent"));
                onlineUser.setBrowser(userAgent.getBrowser().getName());
                onlineUser.setOs(userAgent.getOperatingSystem().getName());
            } catch (Exception e) {
                // Ignore parsing errors
                onlineUser.setBrowser("Unknown");
                onlineUser.setOs("Unknown");
            }

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
    public SmsAuthenticationFilter smsAuthenticationFilter(AuthenticationManager authenticationManager) {
        SmsAuthenticationFilter filter = new SmsAuthenticationFilter("/admin/auth/login/sms");
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }

    @Bean
    public EmailAuthenticationFilter emailAuthenticationFilter(AuthenticationManager authenticationManager) {
        EmailAuthenticationFilter filter = new EmailAuthenticationFilter("/admin/auth/login/email");
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }

    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager) {
        JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(
                "/admin/auth/login");
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        filter.setPrivateKey(privateKey);
        return filter;
    }

}
