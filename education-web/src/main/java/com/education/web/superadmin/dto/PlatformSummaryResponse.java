package com.education.web.superadmin.dto;

/**
 * Агрегати платформи для карток загального дашборду суперадміна.
 */
public record PlatformSummaryResponse(
        int students,
        int teachers,
        int schools,
        int courses
) {
}
