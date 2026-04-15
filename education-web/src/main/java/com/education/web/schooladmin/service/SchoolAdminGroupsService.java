package com.education.web.schooladmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.UserEntity;
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

    private static final int MAX_GROUP_CODE_LENGTH = 255;

    private static final DateTimeFormatter INPUT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter OUTPUT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final OrganizationJpaRepository organizations;
    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolSubjectJpaRepository schoolSubjects;
    private final TeacherJpaRepository teachers;
    private final GroupEnrollmentCountService groupEnrollmentCounts;

    public SchoolAdminGroupsService(
            OrganizationJpaRepository organizations,
            SchoolGroupJpaRepository schoolGroups,
            SchoolSubjectJpaRepository schoolSubjects,
            TeacherJpaRepository teachers,
            GroupEnrollmentCountService groupEnrollmentCounts
    ) {
        this.organizations = organizations;
        this.schoolGroups = schoolGroups;
        this.schoolSubjects = schoolSubjects;
        this.teachers = teachers;
        this.groupEnrollmentCounts = groupEnrollmentCounts;
    }

    /**
     * Створити групу або оновити за парою (organization_id, code).
     * Якщо в тілі передано {@code groupId} — оновити існуючий рядок за id (редагування);
     * інакше при зміні поля code upsert за code створив би другу групу замість перезапису.
     */
    @Transactional
    public SchoolGroupCardResponse createGroup(String schoolId, CreateSchoolGroupRequest req) {
        OrganizationEntity org = organizations.findById(schoolId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found")
        );

        LocalDate startDate = parseDate(req.getStartDate(), "startDate");
        LocalDate endDate = parseDate(req.getEndDate(), "endDate");

        String groupIdRaw = Optional.ofNullable(req.getGroupId())
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse("");
        SchoolGroupEntity entity;

        if (!groupIdRaw.isBlank()) {
            entity = schoolGroups.findById(groupIdRaw)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
            if (!entity.getOrganization().getId().equals(schoolId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group does not belong to this school");
            }
            String newCode = req.getCode() != null ? req.getCode().trim() : "";
            if (!newCode.equals(entity.getCode())) {
                schoolGroups.findByOrganization_IdAndCode(schoolId, newCode)
                        .filter(g -> !g.getId().equals(groupIdRaw))
                        .ifPresent(ignored -> {
                            throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Group code already exists for this school"
                            );
                        });
            }
        } else {
            String code = req.getCode() != null ? req.getCode().trim() : "";
            if (code.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing code");
            }
            if (code.length() > MAX_GROUP_CODE_LENGTH) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Group code is too long (max " + MAX_GROUP_CODE_LENGTH + " characters)"
                );
            }

            Optional<SchoolGroupEntity> existingOpt =
                    schoolGroups.findByOrganization_IdAndCode(schoolId, code);

            entity = existingOpt.orElseGet(() -> {
                SchoolGroupEntity e = new SchoolGroupEntity();
                e.setOrganization(org);
                return e;
            });
        }

        applyPayloadToEntity(entity, schoolId, req, startDate, endDate);

        SchoolGroupEntity saved = schoolGroups.save(entity);
        return toCard(saved, groupEnrollmentCounts.countForGroup(saved.getId()));
    }

    private void applyPayloadToEntity(
            SchoolGroupEntity entity,
            String schoolId,
            CreateSchoolGroupRequest req,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String name = req.getName() != null ? req.getName().trim() : "";
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing name");
        }
        entity.setName(name);

        String code = req.getCode() != null ? req.getCode().trim() : "";
        if (code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing code");
        }
        if (code.length() > MAX_GROUP_CODE_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Group code is too long (max " + MAX_GROUP_CODE_LENGTH + " characters)"
            );
        }
        entity.setCode(code);

        String topicsTrimmed = req.getTopicsLabel() == null ? "" : req.getTopicsLabel().trim();
        String sid = req.getSubjectId() != null ? req.getSubjectId().trim() : "";
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

        String tid = req.getTeacherId() != null ? req.getTeacherId().trim() : "";
        if (!tid.isBlank()) {
            TeacherEntity teacher = teachers
                    .findByIdAndSchool_Id(tid, schoolId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Teacher not found for this school"
                    ));
            entity.setTeacher(teacher);
        } else {
            entity.setTeacher(null);
        }

        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setStudentsCount(req.getStudentsCount());
        entity.setActive(req.isActive());
        Boolean showFlag = req.getShowSubjectOnCard();
        entity.setShowSubjectOnCard(showFlag == null || showFlag);
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

    private SchoolGroupCardResponse toCard(SchoolGroupEntity g, int studentsFromEnrollment) {
        String subjectId = g.getSubject() != null ? g.getSubject().getId() : null;
        String teacherId = g.getTeacher() != null ? g.getTeacher().getId() : null;
        return new SchoolGroupCardResponse(
                g.getId(),
                g.getName(),
                g.getCode(),
                subjectId,
                teacherId,
                formatTeacherDisplayName(g),
                g.getTopicsLabel() != null ? g.getTopicsLabel() : "",
                g.getStartDate() != null ? OUTPUT_DATE_FMT.format(g.getStartDate()) : "—",
                g.getEndDate() != null ? OUTPUT_DATE_FMT.format(g.getEndDate()) : "—",
                studentsFromEnrollment,
                g.getHomeworkStarsTotal(),
                g.isActive(),
                g.isShowSubjectOnCard()
        );
    }

    private String formatTeacherDisplayName(SchoolGroupEntity g) {
        if (g.getTeacher() == null) {
            return null;
        }
        UserEntity u = g.getTeacher().getUser();
        if (u == null) {
            return null;
        }
        String first = u.getFirstName() != null ? u.getFirstName().trim() : "";
        String last = u.getLastName() != null ? u.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? null : full;
    }
}
