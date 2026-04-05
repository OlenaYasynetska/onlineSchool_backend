package com.education.domain.student;

import java.time.Instant;
import java.util.Objects;

public final class Student {
    private final StudentId id;
    private final String fullName;
    private final Email email;
    private final SchoolId schoolId;
    /** id з таблиці `users` для логіну; null якщо акаунт ще не створено. */
    private final String linkedUserId;
    private final Instant createdAt;

    private Student(
            StudentId id,
            String fullName,
            Email email,
            SchoolId schoolId,
            String linkedUserId,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.fullName = requireName(fullName);
        this.email = Objects.requireNonNull(email);
        this.schoolId = Objects.requireNonNull(schoolId);
        this.linkedUserId = linkedUserId == null || linkedUserId.isBlank() ? null : linkedUserId.trim();
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Student createNew(String fullName, Email email, SchoolId schoolId, String linkedUserId) {
        return new Student(StudentId.newId(), fullName, email, schoolId, linkedUserId, Instant.now());
    }

    public static Student rehydrate(
            StudentId id,
            String fullName,
            Email email,
            SchoolId schoolId,
            String linkedUserId,
            Instant createdAt
    ) {
        return new Student(id, fullName, email, schoolId, linkedUserId, createdAt);
    }

    public StudentId id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public Email email() {
        return email;
    }

    public SchoolId schoolId() {
        return schoolId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    /** Може бути null для записів без облікового запису. */
    public String linkedUserId() {
        return linkedUserId;
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "fullName must not be null");
        String normalized = name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank");
        }
        return normalized;
    }
}

