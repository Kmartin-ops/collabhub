package com.collabhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // React dev server
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // Standard REST methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow auth header + content type
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Allow frontend to read Authorization header in responses
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies/credentials if needed
        config.setAllowCredentials(true);

        // Cache preflight for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}