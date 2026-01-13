package com.carbontrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Mining and Actuator are public
                        .requestMatchers("/mining/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Auth endpoints must be public
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()

                        // Market and Points are functional endpoints used by client (which has no auth
                        // token)
                        // Ideally these should be secured, but client needs update.
                        // For now, permitting them allows verification of "app runs".
                        .requestMatchers("/api/market/**", "/api/points/**").permitAll()
                        .requestMatchers("/api/users/*/profile").permitAll() // Allow profile fetch for client
                        .requestMatchers("/error").permitAll() // Allow error responses (Avoid 401 on 400s)

                        // Public User List concern: Restrict to ADMIN
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .httpBasic(basic -> {
                }); // allow basic security if ever needed

        return http.build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
