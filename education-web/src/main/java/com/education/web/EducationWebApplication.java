package com.education.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.education")
public class EducationWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationWebApplication.class, args);
    }
}

