package com.education.platform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "students")
public class Student {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;  // reference to User
    @Indexed
    private String schoolId;
    private Set<String> courseIds;
    private String grade;

    private Instant createdAt;
    private Instant updatedAt;
}
