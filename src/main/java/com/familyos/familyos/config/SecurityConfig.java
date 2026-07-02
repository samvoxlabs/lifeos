package com.familyos.familyos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // For POC only: permit all requests so Swagger UI and endpoints are accessible locally.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
        .authorizeHttpRequests(auth -> auth
          .requestMatchers("/", "/llm/test").permitAll()
          .anyRequest().authenticated()
        )
        .oauth2Login(Customizer.withDefaults());
        return http.build();
    }
}
