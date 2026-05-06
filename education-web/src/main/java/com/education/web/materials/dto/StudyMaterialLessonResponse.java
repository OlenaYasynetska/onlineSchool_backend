package com.education.web.materials.dto;

public record StudyMaterialLessonResponse(
        String id,
        String title,
        int sortOrder,
        String fileName
) {}
