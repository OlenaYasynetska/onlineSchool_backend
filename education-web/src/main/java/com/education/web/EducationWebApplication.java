package com.education.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.education")
@EntityScan(basePackages = "com.education")
@EnableJpaRepositories(basePackages = "com.education")
public class EducationWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationWebApplication.class, args);
    }
}

