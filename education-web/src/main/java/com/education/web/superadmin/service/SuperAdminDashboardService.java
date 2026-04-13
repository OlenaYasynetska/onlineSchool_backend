package com.education.web.superadmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.PaymentEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.PaymentJpaRepository;
import com.education.web.auth.repository.SchoolSubjectJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.web.superadmin.dto.OrganizationRowResponse;
import com.education.web.superadmin.dto.PaymentHistoryRowResponse;
import com.education.web.superadmin.dto.PlatformSummaryResponse;
import com.education.web.superadmin.dto.PlanOverviewItemResponse;
import com.education.web.superadmin.dto.SchoolCardResponse;
import com.education.web.superadmin.dto.SuperAdminDashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SuperAdminDashboardService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrganizationJpaRepository organizations;
    private final PaymentJpaRepository payments;
    private final SpringDataStudentJpaRepository students;
    private final UserJpaRepository users;
    private final TeacherJpaRepository teachers;
    private final SchoolSubjectJpaRepository schoolSubjects;

    public SuperAdminDashboardService(
            OrganizationJpaRepository organizations,
            PaymentJpaRepository payments,
            SpringDataStudentJpaRepository students,
            UserJpaRepository users,
            TeacherJpaRepository teachers,
            SchoolSubjectJpaRepository schoolSubjects
    ) {
        this.organizations = organizations;
        this.payments = payments;
        this.students = students;
        this.users = users;
        this.teachers = teachers;
        this.schoolSubjects = schoolSubjects;
    }

    @Transactional(readOnly = true)
    public SuperAdminDashboardResponse getDashboard() {
        List<OrganizationEntity> orgs = organizations.findAllByOrderByRegisteredAtDesc();
        List<PaymentEntity> paymentRows = payments.findAllByOrderByCreatedAtDesc();

        int total = orgs.size();
        int proCount = countByPlanKey(orgs, "pro");
        int standardCount = countByPlanKey(orgs, "standard");
        int freeCount = countByPlanKey(orgs, "free");

        List<PlanOverviewItemResponse> overview = List.of(
                new PlanOverviewItemResponse("pro", "Plan Pro", proCount, percent(proCount, total)),
                new PlanOverviewItemResponse("standard", "Plan Standard", standardCount, percent(standardCount, total)),
                new PlanOverviewItemResponse("free", "Plan Free", freeCount, percent(freeCount, total))
        );

        Map<String, Integer> studentCountByOrgId = studentCountsByOrgId(orgs);

        List<SchoolCardResponse> schoolCards = new ArrayList<>();
        for (int i = 0; i < orgs.size(); i++) {
            OrganizationEntity o = orgs.get(i);
            String title = "School " + (i + 1);
            int studentCount = studentCountByOrgId.getOrDefault(o.getId(), 0);
            schoolCards.add(new SchoolCardResponse(
                    o.getId(),
                    title,
                    schoolAdminDisplayName(o),
                    formatAddress(o),
                    planTitle(o.getPlan().getPlanKey()),
                    studentCount
            ));
        }

        List<OrganizationRowResponse> organizationsResponse = orgs.stream()
                .map(o -> new OrganizationRowResponse(
                        o.getId(),
                        o.getName(),
                        planTitle(o.getPlan().getPlanKey()),
                        normalizeStatus(o.getStatus()),
                        o.getNextBillingAt() == null ? "—" : o.getNextBillingAt().toLocalDate().toString(),
                        o.getRegisteredAt() == null ? "—" : DATE_FMT.format(o.getRegisteredAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                        "$" + o.getTotalReceived().setScale(2, RoundingMode.HALF_UP),
                        formatAddress(o),
                        studentCountByOrgId.getOrDefault(o.getId(), 0)
                ))
                .toList();

        List<PaymentHistoryRowResponse> paymentsResponse = paymentRows.stream()
                .map(p -> new PaymentHistoryRowResponse(
                        p.getId(),
                        p.getCreatedAt() == null ? "—" : DATE_FMT.format(p.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                        p.getOrganization().getName(),
                        "$" + p.getAmount().setScale(2, RoundingMode.HALF_UP),
                        normalizePaymentStatus(p.getStatus())
                ))
                .toList();

        int totalStudents = studentCountByOrgId.values().stream().mapToInt(Integer::intValue).sum();
        int totalSchools = orgs.size();
        int totalTeachers = (int) Math.min(teachers.count(), Integer.MAX_VALUE);
        int totalCourses = (int) Math.min(schoolSubjects.count(), Integer.MAX_VALUE);
        PlatformSummaryResponse summary = new PlatformSummaryResponse(
                totalStudents,
                totalTeachers,
                totalSchools,
                totalCourses
        );

        return new SuperAdminDashboardResponse(
                overview,
                schoolCards,
                organizationsResponse,
                paymentsResponse,
                summary
        );
    }

    /**
     * Кількість учнів по {@code students.school_id} для кожної організації.
     * Один запит GROUP BY замість N × countBySchoolId.
     */
    private Map<String, Integer> studentCountsByOrgId(List<OrganizationEntity> orgs) {
        Map<String, Integer> map = new HashMap<>();
        if (orgs.isEmpty()) {
            return map;
        }
        List<String> ids = orgs.stream().map(OrganizationEntity::getId).toList();
        List<Object[]> rows = students.countBySchoolIdsGrouped(ids);
        for (Object[] row : rows) {
            if (row.length >= 2 && row[0] != null && row[1] != null) {
                long n = ((Number) row[1]).longValue();
                map.put((String) row[0], (int) Math.min(n, Integer.MAX_VALUE));
            }
        }
        for (OrganizationEntity o : orgs) {
            map.putIfAbsent(o.getId(), 0);
        }
        return map;
    }

    /** Помаранчева підпис на картці: ім'я адміністратора з реєстрації, інакше назва організації. */
    private String schoolAdminDisplayName(OrganizationEntity o) {
        String fallback = o.getName();
        if (o.getAdminUserId() == null || o.getAdminUserId().isBlank()) {
            return fallback;
        }
        Optional<UserEntity> admin = users.findById(o.getAdminUserId());
        if (admin.isEmpty()) {
            return fallback;
        }
        UserEntity u = admin.get();
        String full = (u.getFirstName() + " " + u.getLastName()).trim();
        return full.isEmpty() ? fallback : full;
    }

    private String formatAddress(OrganizationEntity o) {
        String a = o.getAddress() == null ? "" : o.getAddress().trim();
        String c = o.getCountry() == null ? "" : o.getCountry().trim();
        if (a.isEmpty() && c.isEmpty()) {
            return "—";
        }
        if (a.isEmpty()) {
            return c;
        }
        if (c.isEmpty()) {
            return a;
        }
        return a + "\n" + c;
    }

    private int countByPlanKey(List<OrganizationEntity> orgs, String key) {
        return (int) orgs.stream()
                .filter(o -> o.getPlan() != null && key.equalsIgnoreCase(o.getPlan().getPlanKey()))
                .count();
    }

    private int percent(int part, int total) {
        if (total == 0) return 0;
        return (int) Math.round(part * 100.0 / total);
    }

    private String planTitle(String planKey) {
        return switch (planKey == null ? "" : planKey.toLowerCase()) {
            case "pro" -> "Pro";
            case "standard" -> "Standard";
            default -> "Free";
        };
    }

    private String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) return "Inactive";
        return switch (raw.trim().toLowerCase()) {
            case "active" -> "Active";
            case "expiring", "expiring soon", "pending" -> "Expiring soon";
            default -> "Inactive";
        };
    }

    private String normalizePaymentStatus(String raw) {
        if (raw == null || raw.isBlank()) return "Pending payment";
        return switch (raw.trim().toLowerCase()) {
            case "paid", "success", "successful" -> "Paid";
            case "failed", "error" -> "Failed";
            default -> "Pending payment";
        };
    }
}

