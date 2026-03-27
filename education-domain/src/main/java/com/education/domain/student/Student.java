package com.education.domain.student;

import java.time.Instant;
import java.util.Objects;

public final class Student {
    private final StudentId id;
    private final String fullName;
    private final Email email;
    private final SchoolId schoolId;
    private final Instant createdAt;

    private Student(StudentId id, String fullName, Email email, SchoolId schoolId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.fullName = requireName(fullName);
        this.email = Objects.requireNonNull(email);
        this.schoolId = Objects.requireNonNull(schoolId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Student createNew(String fullName, Email email, SchoolId schoolId) {
        return new Student(StudentId.newId(), fullName, email, schoolId, Instant.now());
    }

    public static Student rehydrate(StudentId id, String fullName, Email email, SchoolId schoolId, Instant createdAt) {
        return new Student(id, fullName, email, schoolId, createdAt);
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

    private static String requireName(String name) {
        Objects.requireNonNull(name, "fullName must not be null");
        String normalized = name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank");
        }
        return normalized;
    }
}

