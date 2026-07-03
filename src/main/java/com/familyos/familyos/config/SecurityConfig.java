package com.familyos.familyos.config;

import com.familyos.familyos.authentication.filter.JwtAuthenticationFilter;
import com.familyos.familyos.authentication.handler.RestAccessDeniedHandler;
import com.familyos.familyos.authentication.handler.RestAuthenticationEntryPoint;
import com.familyos.familyos.authentication.oauth.OAuthSuccessHandler;
import com.familyos.familyos.authentication.service.JwtService;
import com.familyos.familyos.authentication.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final OAuthSuccessHandler oauthSuccessHandler;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtService jwtService, OAuthSuccessHandler oauthSuccessHandler,
                          UserService userService, ObjectMapper objectMapper,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtService = jwtService;
        this.oauthSuccessHandler = oauthSuccessHandler;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/**", "/login/**", "/error", "/actuator/health", "/api/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauthSuccessHandler)
            )
            .addFilterBefore(
                jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userService, objectMapper);
    }
}
