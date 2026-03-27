package com.education.domain.student;

import java.util.Objects;

public record Email(String value) {
    public Email {
        Objects.requireNonNull(value, "email must not be null");
        String normalized = value.trim().toLowerCase();
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        value = normalized;
    }
}

