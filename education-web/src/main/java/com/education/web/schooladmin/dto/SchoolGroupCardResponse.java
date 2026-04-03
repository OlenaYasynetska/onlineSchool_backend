package com.education.web.schooladmin.dto;

public record SchoolGroupCardResponse(
        String id,
        String name,
        String code,
        String subjectId,
        String teacherId,
        String topicsLabel,
        String startDate,
        String endDate,
        int studentsCount,
        boolean active
) {
}
