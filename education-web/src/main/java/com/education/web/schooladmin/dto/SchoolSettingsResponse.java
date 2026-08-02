package com.education.web.schooladmin.dto;

/** Налаштування школи (організації). */
public record SchoolSettingsResponse(
        String schoolId,
        String gradingMethod,
        String gradingScale
) {
}
