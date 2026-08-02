package com.education.web.homework.dto;

/**
 * Агрегат по предмету: скільки ДЗ здано, скільки з них уже оцінено, показник з оцінених.
 */
public record SubjectHomeworkProgressRow(
        String subject,
        int submittedCount,
        int gradedCount,
        double starsTotal
) {
}
