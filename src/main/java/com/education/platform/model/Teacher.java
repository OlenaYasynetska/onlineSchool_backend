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
@Document(collection = "teachers")
public class Teacher {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;
    @Indexed
    private String schoolId;
    private Set<String> courseIds;
    private String subject;

    private Instant createdAt;
    private Instant updatedAt;
}
