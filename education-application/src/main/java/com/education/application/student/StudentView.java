package com.education.application.student;

import java.time.Instant;

public record StudentView(
        String id,
        String fullName,
        String email,
        String schoolId,
        Instant createdAt,
        boolean inviteEmailSent
) {
    public StudentView(String id, String fullName, String email, String schoolId, Instant createdAt) {
        this(id, fullName, email, schoolId, createdAt, false);
    }
}

