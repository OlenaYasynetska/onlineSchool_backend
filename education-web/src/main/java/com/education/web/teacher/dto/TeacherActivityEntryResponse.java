package com.education.web.teacher.dto;

/**
 * Подія для журналу активності вчителя (наприклад, зарахування на групу).
 */
public record TeacherActivityEntryResponse(
        String date,
        String studentName,
        int change,
        String reason
) {
}
