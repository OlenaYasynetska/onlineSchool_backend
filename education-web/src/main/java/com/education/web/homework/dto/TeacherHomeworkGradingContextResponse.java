package com.education.web.homework.dto;

/** Налаштування оцінювання ДЗ для школи вчителя. */
public record TeacherHomeworkGradingContextResponse(
        String gradingMethod,
        String gradingScale,
        int minGrade,
        int maxGrade
) {
}
