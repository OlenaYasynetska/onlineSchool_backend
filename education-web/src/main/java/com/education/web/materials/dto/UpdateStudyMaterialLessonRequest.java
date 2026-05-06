package com.education.web.materials.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStudyMaterialLessonRequest(
        @NotBlank @Size(max = 255) String title
) {}
