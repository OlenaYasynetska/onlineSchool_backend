package com.education.web.materials.dto;

import java.time.Instant;

public record StudyMaterialSetResponse(
        String id,
        String title,
        String description,
        String teacherSubjectId,
        String teacherSubjectTitle,
        int lessonCount,
        Instant updatedAt
) {}
