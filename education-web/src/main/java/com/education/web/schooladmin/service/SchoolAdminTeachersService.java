package com.education.web.schooladmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolSubjectEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.TeacherSubjectEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolSubjectJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.TeacherSubjectJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.mail.AccountInvitationMailService;
import com.education.web.schooladmin.dto.CreateSchoolTeacherRequest;
import com.education.web.schooladmin.dto.SchoolTeacherOptionResponse;
import com.education.web.util.SecurePasswordGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SchoolAdminTeachersService {

    private static final int MIN_PASSWORD_LEN = 8;
    private static final int GENERATED_PASSWORD_LEN = 12;
    private static final int MAX_SUBJECT_TITLE_LEN = 255;

    private final OrganizationJpaRepository organizations;
    private final SchoolSubjectJpaRepository schoolSubjects;
    private final TeacherJpaRepository teachers;
    private final TeacherSubjectJpaRepository teacherSubjects;
    private final SchoolGroupJpaRepository schoolGroups;
    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AccountInvitationMailService invitationMailService;

    public SchoolAdminTeachersService(
            OrganizationJpaRepository organizations,
            SchoolSubjectJpaRepository schoolSubjects,
            TeacherJpaRepository teachers,
            TeacherSubjectJpaRepository teacherSubjects,
            SchoolGroupJpaRepository schoolGroups,
            UserJpaRepository users,
            PasswordEncoder passwordEncoder,
            AccountInvitationMailService invitationMailService
    ) {
        this.organizations = organizations;
        this.schoolSubjects = schoolSubjects;
        this.teachers = teachers;
        this.teacherSubjects = teacherSubjects;
        this.schoolGroups = schoolGroups;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.invitationMailService = invitationMailService;
    }

    @Transactional(readOnly = true)
    public List<SchoolTeacherOptionResponse> listTeachers(String schoolId) {
        if (organizations.findById(schoolId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found");
        }
        return teachers.findAllBySchoolIdWithUserOrderByName(schoolId).stream()
                .map(t -> toOption(t, false))
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

        String rawPassword = req.password() != null ? req.password().trim() : "";
        if (rawPassword.isEmpty()) {
            rawPassword = SecurePasswordGenerator.random(GENERATED_PASSWORD_LEN);
        } else if (rawPassword.length() < MIN_PASSWORD_LEN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least " + MIN_PASSWORD_LEN + " characters, or leave empty to generate"
            );
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(first);
        user.setLastName(last);
        user.setRole(UserRole.TEACHER);
        user.setEnabled(true);
        String phoneRaw = req.phone();
        if (phoneRaw != null && !phoneRaw.isBlank()) {
            user.setPhone(phoneRaw.trim());
        } else {
            user.setPhone(null);
        }
        // INSERT у `users` має бути в БД до INSERT у `teachers` (FK `user_id`), інакше MySQL 1452.
        user = users.saveAndFlush(user);

        TeacherEntity row = new TeacherEntity();
        row.setUser(user);
        row.setSchool(org);
        row = teachers.saveAndFlush(row);

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
            // Каталог школи: той самий предмет з’являється в `school_subjects` (як у «Add course»).
            SchoolSubjectEntity catalog = ensureSchoolSubject(org, title);
            TeacherSubjectEntity line = new TeacherSubjectEntity();
            line.setTeacher(row);
            line.setTitle(catalog.getTitle());
            line.setSortOrder(order++);
            teacherSubjects.save(line);
        }
        teacherSubjects.flush();

        boolean sendInvite = req.sendInviteEmail() == null || req.sendInviteEmail();
        boolean inviteSent = false;
        if (sendInvite) {
            String display = (first + " " + last).trim();
            inviteSent = invitationMailService.sendInvite(email, display, rawPassword, email, "teacher");
        }

        return toOption(row, inviteSent);
    }

    /** Додає рядок у `school_subjects`, якщо такої назви ще немає (без урахування регістру). */
    private SchoolSubjectEntity ensureSchoolSubject(OrganizationEntity org, String title) {
        return schoolSubjects.findByOrganization_IdAndTitleIgnoreCase(org.getId(), title)
                .orElseGet(() -> {
                    SchoolSubjectEntity e = new SchoolSubjectEntity();
                    e.setOrganization(org);
                    e.setTitle(title);
                    return schoolSubjects.save(e);
                });
    }

    private SchoolTeacherOptionResponse toOption(TeacherEntity t, boolean inviteEmailSent) {
        UserEntity u = t.getUser();
        String display = (u.getFirstName() + " " + u.getLastName()).trim();
        List<String> subjects = teacherSubjects.findByTeacher_IdOrderBySortOrderAsc(t.getId()).stream()
                .map(TeacherSubjectEntity::getTitle)
                .toList();
        List<String> groups = schoolGroups.findByTeacher_IdOrderByNameAsc(t.getId()).stream()
                .map(SchoolGroupEntity::getName)
                .toList();
        String phone = u.getPhone();
        if (phone != null) {
            phone = phone.trim();
            if (phone.isEmpty()) {
                phone = null;
            }
        }
        return new SchoolTeacherOptionResponse(
                t.getId(),
                display,
                u.getEmail(),
                phone,
                subjects,
                groups,
                inviteEmailSent
        );
    }
}
