package com.education.domain.student;

import java.util.Objects;

public record SchoolId(String value) {
    public SchoolId {
        Objects.requireNonNull(value, "schoolId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("schoolId must not be blank");
        }
    }
}

