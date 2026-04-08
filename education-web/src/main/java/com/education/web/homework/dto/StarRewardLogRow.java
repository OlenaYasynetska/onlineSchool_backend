package com.education.web.homework.dto;

import java.time.Instant;

/** Один рядок журналу: оцінка ДЗ. */
public record StarRewardLogRow(
        Instant gradedAt,
        String teacherName,
        String subject,
        int stars,
        String feedback
) {
}
