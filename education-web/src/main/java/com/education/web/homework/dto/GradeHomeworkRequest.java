package com.education.web.homework.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradeHomeworkRequest(
        @NotNull @Min(1) @Max(3) Integer stars,
        String feedback
) {
}
