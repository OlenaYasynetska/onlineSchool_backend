package com.education.web.schooladmin.dto;

import java.util.List;

public record StudentRowResponse(
        String id,
        String fullName,
        String email,
        String joinedAt,
        List<String> groupNames
) {
}

