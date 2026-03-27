package com.education.domain.student;

import java.util.Objects;
import java.util.UUID;

public record StudentId(String value) {
    public StudentId {
        Objects.requireNonNull(value, "studentId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("studentId must not be blank");
        }
    }

    public static StudentId newId() {
        return new StudentId(UUID.randomUUID().toString());
    }
}

