package com.education.web.schooladmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolSubjectEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolSubjectJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.schooladmin.dto.CreateSchoolGroupRequest;
import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class SchoolAdminGroupsService {

    private static final DateTimeFormatter INPUT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter OUTPUT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final OrganizationJpaRepository organizations;
    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolSubjectJpaRepository schoolSubjects;
    private final TeacherJpaRepository teachers;

    public SchoolAdminGroupsService(
            OrganizationJpaRepository organizations,
            SchoolGroupJpaRepository schoolGroups,
            SchoolSubjectJpaRepository schoolSubjects,
            TeacherJpaRepository teachers
    ) {
        this.organizations = organizations;
        this.schoolGroups = schoolGroups;
        this.schoolSubjects = schoolSubjects;
        this.teachers = teachers;
    }

    /**
     * Create (upsert) group for a school.
     * If (organization_id, code) already exists - we update existing record.
     */
    @Transactional
    public SchoolGroupCardResponse createGroup(String schoolId, CreateSchoolGroupRequest req) {
        OrganizationEntity org = organizations.findById(schoolId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found")
        );

        LocalDate startDate = parseDate(req.startDate(), "startDate");
        LocalDate endDate = parseDate(req.endDate(), "endDate");

        String code = req.code() != null ? req.code().trim() : "";
        if (code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing code");
        }

        String name = req.name() != null ? req.name().trim() : "";
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing name");
        }

        Optional<SchoolGroupEntity> existingOpt =
                schoolGroups.findByOrganization_IdAndCode(schoolId, code);

        SchoolGroupEntity entity = existingOpt.orElseGet(() -> {
            SchoolGroupEntity e = new SchoolGroupEntity();
            e.setOrganization(org);
            return e;
        });

        entity.setName(name);
        entity.setCode(code);

        String topicsTrimmed = req.topicsLabel() == null ? "" : req.topicsLabel().trim();
        String sid = req.subjectId() != null ? req.subjectId().trim() : "";
        if (!sid.isBlank()) {
            SchoolSubjectEntity subj = schoolSubjects
                    .findByIdAndOrganization_Id(sid, schoolId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Subject not found for this school"
                    ));
            entity.setSubject(subj);
            entity.setTopicsLabel(subj.getTitle());
        } else {
            entity.setSubject(null);
            if (topicsTrimmed.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Missing topics label or subject"
                );
            }
            entity.setTopicsLabel(topicsTrimmed);
        }

        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setStudentsCount(req.studentsCount());
        entity.setActive(req.active());

        SchoolGroupEntity saved = schoolGroups.save(entity);
        return toCard(saved);
    }

    private LocalDate parseDate(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + field);
        }
        try {
            return LocalDate.parse(raw.trim(), INPUT_DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid " + field + " format. Expected dd.MM.yyyy"
            );
        }
    }

    private SchoolGroupCardResponse toCard(SchoolGroupEntity g) {
        String subjectId = g.getSubject() != null ? g.getSubject().getId() : null;
        String teacherId = g.getTeacher() != null ? g.getTeacher().getId() : null;
        return new SchoolGroupCardResponse(
                g.getId(),
                g.getName(),
                g.getCode(),
                subjectId,
                teacherId,
                g.getTopicsLabel() != null ? g.getTopicsLabel() : "",
                g.getStartDate() != null ? OUTPUT_DATE_FMT.format(g.getStartDate()) : "—",
                g.getEndDate() != null ? OUTPUT_DATE_FMT.format(g.getEndDate()) : "—",
                g.getStudentsCount(),
                g.isActive()
        );
    }
}
