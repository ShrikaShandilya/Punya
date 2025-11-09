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

                // Mining subsystem is fully public
                .requestMatchers("/mining/**").permitAll()

                // All API endpoints are public
                .requestMatchers("/api/**").permitAll()

                // Actuator for health/info is public
                .requestMatchers("/actuator/**").permitAll()

                // Everything else (UI, static, fallback) also allowed
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {}); // allow basic security if ever needed

        return http.build();
    }
}
