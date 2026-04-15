package com.education.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class CorsConfig {

    /**
     * <p>{@code CORS_ALLOWED_ORIGINS} — через кому (прод: URL фронту на Vercel тощо).</p>
     * <p>Якщо в env задано хоча б один origin і {@code merge-localhost=true}, додаються
     * {@code http://localhost:4200} та {@code http://127.0.0.1:4200}.</p>
     * <p>Якщо {@code CORS_ALLOWED_ORIGINS} порожній — fallback: {@code *} та {@code credentials=false}.</p>
     * <p>Якщо список explicit origin непорожній — {@code allowCredentials(true)} (не сумісно з wildcard "*").</p>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String allowedOriginsRaw,
            @Value("${app.cors.merge-localhost:true}") boolean mergeLocalhost
    ) {
        CorsConfiguration config = new CorsConfiguration();
        Set<String> fromEnv = new LinkedHashSet<>();
        for (String part : allowedOriginsRaw.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                fromEnv.add(t);
            }
        }

        /*
         * Раніше при порожньому CORS_ALLOWED_ORIGINS і merge-localhost=true у список потрапляли лише
         * localhost — прод (напр. Vercel) отримував Invalid CORS / 403. Явні origin з env збираються
         * окремо; merge-localhost застосовується лише коли env не порожній.
         */
        if (fromEnv.isEmpty()) {
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowCredentials(false);
        } else {
            Set<String> origins = new LinkedHashSet<>(fromEnv);
            if (mergeLocalhost) {
                origins.add("http://localhost:4200");
                origins.add("http://127.0.0.1:4200");
            }
            config.setAllowedOrigins(new ArrayList<>(origins));
            config.setAllowCredentials(true);
        }
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
