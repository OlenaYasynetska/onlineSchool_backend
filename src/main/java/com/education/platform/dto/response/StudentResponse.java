package com.education.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private String id;
    private String userId;
    private String schoolId;
    private Set<String> courseIds;
    private String grade;
    private Instant createdAt;
}
