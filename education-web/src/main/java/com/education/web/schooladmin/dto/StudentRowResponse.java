package com.education.web.schooladmin.dto;

public record StudentRowResponse(
        String id,
        String fullName,
        String email,
        String joinedAt
) {
}

