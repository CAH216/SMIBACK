package com.smiback.smiback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://supermarcheinternational.onrender.com",
                        "https://supermarcheinternational.com",
                        "http://127.0.0.1:8000",
                        "http://localhost:8000"  // Ajoutez localhost aussi
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")  // Ajout de HEAD et PATCH
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")  // Headers exposés au client
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight pendant 1 heure
    }
}