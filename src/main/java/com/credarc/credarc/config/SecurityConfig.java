package com.credarc.credarc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery) because we will use JWTs, not cookies.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Set session management to STATELESS. Spring will no longer remember sessions.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Configure Endpoint access rules
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to access the auth endpoints (signup, login)
                        .requestMatchers("/auth/**","/error").permitAll()

                        // EVERY other request requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}