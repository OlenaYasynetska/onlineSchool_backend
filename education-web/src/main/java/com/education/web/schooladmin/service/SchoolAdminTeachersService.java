package com.education.web.schooladmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.TeacherSubjectEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.TeacherSubjectJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.schooladmin.dto.CreateSchoolTeacherRequest;
import com.education.web.schooladmin.dto.SchoolTeacherOptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SchoolAdminTeachersService {

    private static final int MIN_PASSWORD_LEN = 8;
    private static final int MAX_SUBJECT_TITLE_LEN = 255;

    private final OrganizationJpaRepository organizations;
    private final TeacherJpaRepository teachers;
    private final TeacherSubjectJpaRepository teacherSubjects;
    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;

    public SchoolAdminTeachersService(
            OrganizationJpaRepository organizations,
            TeacherJpaRepository teachers,
            TeacherSubjectJpaRepository teacherSubjects,
            UserJpaRepository users,
            PasswordEncoder passwordEncoder
    ) {
        this.organizations = organizations;
        this.teachers = teachers;
        this.teacherSubjects = teacherSubjects;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<SchoolTeacherOptionResponse> listTeachers(String schoolId) {
        if (organizations.findById(schoolId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found");
        }
        return teachers.findAllBySchoolIdWithUserOrderByName(schoolId).stream()
                .map(this::toOption)
                .toList();
    }

    @Transactional
    public SchoolTeacherOptionResponse createTeacher(String schoolId, CreateSchoolTeacherRequest req) {
        OrganizationEntity org = organizations.findById(schoolId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found")
        );

        String email = req.email() != null ? req.email().trim().toLowerCase() : "";
        if (email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing email");
        }
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        String first = req.firstName() != null ? req.firstName().trim() : "";
        String last = req.lastName() != null ? req.lastName().trim() : "";
        if (first.isBlank() || last.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing first or last name");
        }

        String rawPassword = req.password() != null ? req.password() : "";
        if (rawPassword.length() < MIN_PASSWORD_LEN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least " + MIN_PASSWORD_LEN + " characters"
            );
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(first);
        user.setLastName(last);
        user.setRole(UserRole.TEACHER);
        user.setEnabled(true);
        // INSERT у `users` має бути в БД до INSERT у `teachers` (FK `user_id`), інакше MySQL 1452.
        user = users.saveAndFlush(user);

        TeacherEntity row = new TeacherEntity();
        row.setUser(user);
        row.setSchool(org);
        row = teachers.save(row);

        List<String> titles = req.subjects() != null ? req.subjects() : List.of();
        int order = 0;
        for (String raw : titles) {
            if (raw == null) {
                continue;
            }
            String title = raw.trim();
            if (title.isEmpty()) {
                continue;
            }
            if (title.length() > MAX_SUBJECT_TITLE_LEN) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Each subject must be at most " + MAX_SUBJECT_TITLE_LEN + " characters"
                );
            }
            TeacherSubjectEntity line = new TeacherSubjectEntity();
            line.setTeacher(row);
            line.setTitle(title);
            line.setSortOrder(order++);
            teacherSubjects.save(line);
        }

        return toOption(row);
    }

    private SchoolTeacherOptionResponse toOption(TeacherEntity t) {
        UserEntity u = t.getUser();
        String display = (u.getFirstName() + " " + u.getLastName()).trim();
        return new SchoolTeacherOptionResponse(t.getId(), display);
    }
}
