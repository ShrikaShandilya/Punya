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
                // Public mining API
                .requestMatchers("/mining/**").permitAll()

                // Public user and market APIs
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/market/**").permitAll()

                // Public actuator endpoints
                .requestMatchers("/actuator/**").permitAll()

                // Everything else requires authentication
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
