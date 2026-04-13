package com.education.web.superadmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.superadmin.dto.SchoolAdminContactResponse;
import com.education.web.superadmin.dto.SchoolAdminUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SuperAdminSchoolAdminsService {
    private static final DateTimeFormatter REG_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final OrganizationJpaRepository organizations;
    private final UserJpaRepository users;

    public SuperAdminSchoolAdminsService(
            OrganizationJpaRepository organizations,
            UserJpaRepository users
    ) {
        this.organizations = organizations;
        this.users = users;
    }

    /**
     * Усі акаунти з роллю ADMIN_SCHOOL (ім'я та email з реєстрації в {@link UserEntity}),
     * до кожного підставляється назва школи з організації за {@code adminUserId}.
     */
    @Transactional(readOnly = true)
    public List<SchoolAdminContactResponse> listSchoolAdmins() {
        List<UserEntity> admins = users.findAllByRoleOrderByCreatedAtDesc(UserRole.ADMIN_SCHOOL);
        List<SchoolAdminContactResponse> rows = new ArrayList<>();
        for (UserEntity u : admins) {
            String fullName = (u.getFirstName() + " " + u.getLastName()).trim();
            if (fullName.isEmpty()) {
                fullName = "—";
            }
            String schoolName = organizations
                    .findByAdminUserId(u.getId())
                    .map(OrganizationEntity::getName)
                    .orElse("—");
            String login = loginFromEmail(u.getEmail());
            String reg = u.getCreatedAt() == null
                    ? "—"
                    : REG_DATE.format(u.getCreatedAt());
            rows.add(new SchoolAdminContactResponse(
                    u.getId(),
                    fullName,
                    schoolName,
                    u.getEmail(),
                    login,
                    reg,
                    blankToEmpty(u.getSuperAdminNotes())
            ));
        }
        return rows;
    }

    @Transactional
    public SchoolAdminContactResponse updateSchoolAdmin(String userId, SchoolAdminUpdateRequest body) {
        UserEntity user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.ADMIN_SCHOOL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a school administrator");
        }

        String newEmail = resolveEmail(body.email(), body.login(), user.getEmail());
        if (!newEmail.equalsIgnoreCase(user.getEmail())) {
            users.findByEmailIgnoreCase(newEmail).ifPresent(other -> {
                if (!other.getId().equals(userId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                }
            });
            user.setEmail(newEmail);
        }

        applyFullName(user, body.fullName());
        user.setSuperAdminNotes(normalizeNotes(body.notes()));
        users.save(user);

        String schoolName;
        var orgOpt = organizations.findByAdminUserId(userId);
        if (orgOpt.isPresent()) {
            OrganizationEntity org = orgOpt.get();
            if (body.schoolName() != null && !body.schoolName().isBlank()) {
                org.setName(body.schoolName().trim());
                organizations.save(org);
            }
            schoolName = org.getName();
        } else {
            schoolName = "—";
        }

        return toResponse(user, schoolName);
    }

    private SchoolAdminContactResponse toResponse(UserEntity u, String schoolName) {
        String fullName = (u.getFirstName() + " " + u.getLastName()).trim();
        if (fullName.isEmpty()) {
            fullName = "—";
        }
        String reg = u.getCreatedAt() == null
                ? "—"
                : REG_DATE.format(u.getCreatedAt());
        return new SchoolAdminContactResponse(
                u.getId(),
                fullName,
                schoolName,
                u.getEmail(),
                loginFromEmail(u.getEmail()),
                reg,
                blankToEmpty(u.getSuperAdminNotes())
        );
    }

    private static String normalizeNotes(String notes) {
        if (notes == null) {
            return null;
        }
        String t = notes.trim();
        return t.isEmpty() ? null : t;
    }

    private static String blankToEmpty(String s) {
        return s == null || s.isBlank() ? "" : s;
    }

    private static void applyFullName(UserEntity user, String fullName) {
        String raw = fullName == null ? "" : fullName.trim();
        if (raw.isEmpty() || raw.equals("—")) {
            user.setFirstName("—");
            user.setLastName("");
            return;
        }
        int sp = raw.indexOf(' ');
        if (sp < 0) {
            user.setFirstName(raw);
            user.setLastName("");
        } else {
            user.setFirstName(raw.substring(0, sp).trim());
            user.setLastName(raw.substring(sp + 1).trim());
        }
    }

    private static String resolveEmail(String emailField, String loginField, String currentEmail) {
        String e = emailField == null ? "" : emailField.trim();
        String l = loginField == null ? "" : loginField.trim();
        if (!e.isEmpty() && e.contains("@")) {
            return e.toLowerCase();
        }
        if (l.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid email or login is required");
        }
        String domain = extractDomain(currentEmail);
        return (l + "@" + domain).toLowerCase();
    }

    private static String extractDomain(String email) {
        if (email == null || email.isBlank()) {
            return "localhost";
        }
        int at = email.indexOf('@');
        return at > 0 && at < email.length() - 1
                ? email.substring(at + 1).trim()
                : "localhost";
    }

    private static String loginFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return "—";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
