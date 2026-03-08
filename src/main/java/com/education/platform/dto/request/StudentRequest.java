package com.education.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentRequest {

    @NotBlank
    private String userId;
    @NotBlank
    private String schoolId;
    private String grade;
}
