package com.education.web.student;

import com.education.application.student.CreateStudentCommand;
import com.education.application.student.CreateStudentUseCase;
import com.education.application.student.DuplicateStudentEmailException;
import com.education.application.student.StudentView;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.mail.AccountInvitationMailService;
import com.education.web.util.SecurePasswordGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentRegistrationService {

    private static final int MIN_PASSWORD_LEN = 8;
    private static final int GENERATED_PASSWORD_LEN = 12;

    private final OrganizationJpaRepository organizations;
    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;
    private final CreateStudentUseCase createStudentUseCase;
    private final AccountInvitationMailService invitationMailService;

    public StudentRegistrationService(
            OrganizationJpaRepository organizations,
            UserJpaRepository users,
            PasswordEncoder passwordEncoder,
            CreateStudentUseCase createStudentUseCase,
            AccountInvitationMailService invitationMailService
    ) {
        this.organizations = organizations;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.createStudentUseCase = createStudentUseCase;
        this.invitationMailService = invitationMailService;
    }

    @Transactional
    public StudentView register(CreateStudentRequest request) {
        String schoolId = request.schoolId() != null ? request.schoolId().trim() : "";
        if (schoolId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing schoolId");
        }
        organizations.findById(schoolId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found")
        );

        String email = request.email() != null ? request.email().trim().toLowerCase() : "";
        if (email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing email");
        }
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered as a user account");
        }

        String fullName = request.fullName() != null ? request.fullName().trim() : "";
        if (fullName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing full name");
        }

        String rawPassword = request.password() != null ? request.password().trim() : "";
        if (rawPassword.isEmpty()) {
            rawPassword = SecurePasswordGenerator.random(GENERATED_PASSWORD_LEN);
        } else if (rawPassword.length() < MIN_PASSWORD_LEN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least " + MIN_PASSWORD_LEN + " characters, or leave empty to generate"
            );
        }

        String[] names = splitFullName(fullName);
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(names[0]);
        user.setLastName(names[1]);
        user.setRole(UserRole.STUDENT);
        user.setEnabled(true);
        user = users.save(user);

        StudentView created;
        try {
            created = createStudentUseCase.execute(
                    new CreateStudentCommand(fullName, email, schoolId, user.getId())
            );
        } catch (DuplicateStudentEmailException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }

        boolean sendInvite = request.sendInviteEmail() == null || request.sendInviteEmail();
        boolean inviteSent = false;
        if (sendInvite) {
            inviteSent = invitationMailService.sendInvite(
                    email,
                    fullName,
                    rawPassword,
                    email,
                    "student"
            );
        }

        return new StudentView(
                created.id(),
                created.fullName(),
                created.email(),
                created.schoolId(),
                created.createdAt(),
                inviteSent
        );
    }

    private static String[] splitFullName(String fullName) {
        String t = fullName.trim();
        int sp = t.indexOf(' ');
        if (sp <= 0) {
            return new String[] {t, "-"};
        }
        String first = t.substring(0, sp).trim();
        String last = t.substring(sp + 1).trim();
        if (last.isBlank()) {
            last = "-";
        }
        return new String[] {first, last};
    }
}
