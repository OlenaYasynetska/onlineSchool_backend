package com.education.web.superadmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.superadmin.dto.SchoolAdminContactResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    reg
            ));
        }
        return rows;
    }

    private static String loginFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return "—";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
