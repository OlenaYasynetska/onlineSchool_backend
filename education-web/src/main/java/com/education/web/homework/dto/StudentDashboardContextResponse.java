package com.education.web.homework.dto;

import java.util.List;

/** Школа та групи учня з БД (для картки дашборду). */
public record StudentDashboardContextResponse(
        String schoolName,
        List<StudentGroupOptionResponse> groups
) {
}
