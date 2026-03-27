package com.education.application.student;

import java.time.Instant;

public record StudentView(String id, String fullName, String email, String schoolId, Instant createdAt) {
}

