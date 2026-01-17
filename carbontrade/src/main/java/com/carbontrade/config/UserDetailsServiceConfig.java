package com.carbontrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.carbontrade.service.CustomUserDetailsService;

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public CustomUserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }
}