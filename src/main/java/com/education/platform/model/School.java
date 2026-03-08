package com.education.platform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schools")
public class School {

    @Id
    private String id;
    private String name;
    private String description;
    private String address;
    private String adminUserId;  // user with ADMIN_SCHOOL role

    private Instant createdAt;
    private Instant updatedAt;
}
