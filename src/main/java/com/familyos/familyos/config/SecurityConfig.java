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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final OAuthSuccessHandler oauthSuccessHandler;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(JwtService jwtService, OAuthSuccessHandler oauthSuccessHandler,
                          UserService userService, ObjectMapper objectMapper,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.jwtService = jwtService;
        this.oauthSuccessHandler = oauthSuccessHandler;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
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
                .requestMatchers("/oauth2/**", "/login/**", "/error", "/health", "/actuator/health", "/api/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(googleConsentAuthorizationRequestResolver())
                )
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

    @Bean
    public OAuth2AuthorizationRequestResolver googleConsentAuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver delegate =
            new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return withConsent(delegate.resolve(request));
            }

            @Override
            public org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return withConsent(delegate.resolve(request, clientRegistrationId));
            }

            private org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest withConsent(
                org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolved
            ) {
                if (resolved == null) {
                    return null;
                }
                var additionalParameters = new java.util.LinkedHashMap<>(resolved.getAdditionalParameters());
                additionalParameters.put("prompt", "consent");
                return org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
                    .from(resolved)
                    .additionalParameters(additionalParameters)
                    .build();
            }
        };
    }
}
