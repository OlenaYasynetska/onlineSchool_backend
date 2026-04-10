package com.education.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = "com.education",
        exclude = UserDetailsServiceAutoConfiguration.class
)
@EntityScan(basePackages = "com.education")
@EnableJpaRepositories(basePackages = "com.education")
public class EducationWebApplication {

    public static void main(String[] args) {
        // Railway/Render: PORT інколи не підхоплюється плейсхолдером у YAML до ініціалізації — явно в system properties.
        String port = System.getenv("PORT");
        if (port != null && !port.isBlank()) {
            System.setProperty("server.port", port.trim());
        }
        SpringApplication.run(EducationWebApplication.class, args);
    }
}

