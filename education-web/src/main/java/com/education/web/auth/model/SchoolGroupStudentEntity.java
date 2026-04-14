package com.education.web.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "school_group_students")
public class SchoolGroupStudentEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "student_id", nullable = false, length = 36)
    private String studentId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SchoolGroupEntity group;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Для журналу активності вчителя: +1 при зарахуванні в групу (інші значення — за потреби пізніше).
     */
    @Column(name = "change_delta", nullable = false)
    private int changeDelta = 1;

    @PrePersist
    void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public SchoolGroupEntity getGroup() {
        return group;
    }

    public void setGroup(SchoolGroupEntity group) {
        this.group = group;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getChangeDelta() {
        return changeDelta;
    }

    public void setChangeDelta(int changeDelta) {
        this.changeDelta = changeDelta;
    }
}
