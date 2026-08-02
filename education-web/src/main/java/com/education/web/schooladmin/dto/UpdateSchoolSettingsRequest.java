package com.education.web.schooladmin.dto;

/** Оновлення налаштувань школи. */
public record UpdateSchoolSettingsRequest(
        String gradingMethod,
        String gradingScale
) {
}
