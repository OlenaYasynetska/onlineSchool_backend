package com.education.web.schooladmin.service;

import com.education.application.student.GetStudentsBySchoolUseCase;
import com.education.application.student.StudentView;
import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.PaymentEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupStudentEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.PaymentJpaRepository;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import com.education.web.auth.repository.SchoolSubjectJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.schooladmin.dto.PaymentHistoryRowResponse;
import com.education.web.schooladmin.dto.SchoolAdminDashboardResponse;
import com.education.web.schooladmin.dto.SchoolAdminDashboardStatsResponse;
import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import com.education.web.schooladmin.dto.SchoolSubscriptionInfoResponse;
import com.education.web.schooladmin.dto.StudentRowResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SchoolAdminDashboardService {
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** Дата для UI сайдбара (як на макеті dd.MM.yyyy). */
    private static final DateTimeFormatter ACCESS_END_DISPLAY =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final GetStudentsBySchoolUseCase getStudentsBySchoolUseCase;
    private final PaymentJpaRepository payments;
    private final OrganizationJpaRepository organizations;
    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolGroupStudentJpaRepository schoolGroupStudents;
    private final GroupEnrollmentCountService groupEnrollmentCounts;
    private final TeacherJpaRepository teachers;
    private final SchoolSubjectJpaRepository schoolSubjects;

    public SchoolAdminDashboardService(
            GetStudentsBySchoolUseCase getStudentsBySchoolUseCase,
            PaymentJpaRepository payments,
            OrganizationJpaRepository organizations,
            SchoolGroupJpaRepository schoolGroups,
            SchoolGroupStudentJpaRepository schoolGroupStudents,
            GroupEnrollmentCountService groupEnrollmentCounts,
            TeacherJpaRepository teachers,
            SchoolSubjectJpaRepository schoolSubjects
    ) {
        this.getStudentsBySchoolUseCase = getStudentsBySchoolUseCase;
        this.payments = payments;
        this.organizations = organizations;
        this.schoolGroups = schoolGroups;
        this.schoolGroupStudents = schoolGroupStudents;
        this.groupEnrollmentCounts = groupEnrollmentCounts;
        this.teachers = teachers;
        this.schoolSubjects = schoolSubjects;
    }

    @Transactional(readOnly = true)
    public SchoolAdminDashboardResponse getDashboard(String schoolId) {
        List<StudentView> studentViews = getStudentsBySchoolUseCase
                .executeBySchoolId(schoolId);
        List<PaymentEntity> paymentRows = payments
                .findAllByOrganization_IdOrderByCreatedAtDesc(schoolId);

        Map<String, List<String>> groupNamesByStudent =
                groupNamesByStudentId(schoolId);

        List<StudentRowResponse> studentsResponse = studentViews.stream()
                .map(s -> new StudentRowResponse(
                        s.id(),
                        s.fullName(),
                        s.email(),
                        formatDate(s.createdAt()),
                        groupNamesByStudent.getOrDefault(s.id(), List.of())
                ))
                .toList();

        List<PaymentHistoryRowResponse> paymentsResponse = paymentRows.stream()
                .map(p -> new PaymentHistoryRowResponse(
                        p.getId(),
                        formatDate(p.getCreatedAt()),
                        formatAmount(p.getAmount()),
                        p.getCurrency(),
                        normalizePaymentStatus(p.getStatus())
                ))
                .toList();

        int paidCount = (int) paymentsResponse.stream()
                .filter(p -> "Paid".equals(p.status()))
                .count();

        BigDecimal totalReceived = paymentRows.stream()
                .filter(p -> "Paid".equals(normalizePaymentStatus(p.getStatus())))
                .map(PaymentEntity::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SchoolSubscriptionInfoResponse subscription =
                buildSubscriptionInfo(schoolId);

        List<SchoolGroupEntity> groupEntities =
                schoolGroups.findByOrganization_IdOrderByCreatedAtAsc(schoolId);
        Map<String, Long> enrollmentCounts = groupEnrollmentCounts.countsByGroupIds(
                groupEntities.stream().map(SchoolGroupEntity::getId).toList());
        List<SchoolGroupCardResponse> groupsResponse = groupEntities.stream()
                .map(g -> toGroupCard(
                        g,
                        enrollmentCounts.getOrDefault(g.getId(), 0L).intValue()))
                .toList();

        int totalTeachers = (int) teachers.countBySchool_Id(schoolId);
        int totalSubjects = (int) schoolSubjects.countByOrganization_Id(schoolId);

        return new SchoolAdminDashboardResponse(
                schoolId,
                new SchoolAdminDashboardStatsResponse(
                        studentViews.size(),
                        totalTeachers,
                        groupEntities.size(),
                        totalSubjects,
                        paymentRows.size(),
                        paidCount,
                        totalReceived.setScale(2, RoundingMode.HALF_UP).toPlainString()
                ),
                studentsResponse,
                paymentsResponse,
                subscription,
                groupsResponse
        );
    }

    private Map<String, List<String>> groupNamesByStudentId(String schoolId) {
        List<SchoolGroupStudentEntity> links =
                schoolGroupStudents.findByGroup_Organization_Id(schoolId);
        Map<String, List<String>> map = new HashMap<>();
        for (SchoolGroupStudentEntity link : links) {
            String sid = link.getStudentId();
            String name = link.getGroup().getName();
            map.computeIfAbsent(sid, k -> new ArrayList<>()).add(name);
        }
        for (List<String> names : map.values()) {
            names.sort(String::compareToIgnoreCase);
        }
        return map;
    }

    private SchoolGroupCardResponse toGroupCard(SchoolGroupEntity g, int studentsFromEnrollment) {
        LocalDate start = g.getStartDate();
        LocalDate end = g.getEndDate();
        String topics = g.getTopicsLabel() != null ? g.getTopicsLabel() : "";
        String subjectId = g.getSubject() != null ? g.getSubject().getId() : null;
        String teacherId = g.getTeacher() != null ? g.getTeacher().getId() : null;
        String teacherDisplayName = formatTeacherDisplayName(g);
        return new SchoolGroupCardResponse(
                g.getId(),
                g.getName(),
                g.getCode(),
                subjectId,
                teacherId,
                teacherDisplayName,
                topics,
                start != null ? DATE_FMT.format(start) : "—",
                end != null ? DATE_FMT.format(end) : "—",
                studentsFromEnrollment,
                g.getHomeworkStarsTotal(),
                g.isActive()
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

    private SchoolSubscriptionInfoResponse buildSubscriptionInfo(String schoolId) {
        Optional<OrganizationEntity> orgOpt = organizations.findById(schoolId);
        if (orgOpt.isEmpty()) {
            return new SchoolSubscriptionInfoResponse("—", "—");
        }
        OrganizationEntity org = orgOpt.get();
        String planTitle = "—";
        if (org.getPlan() != null && org.getPlan().getTitle() != null
                && !org.getPlan().getTitle().isBlank()) {
            planTitle = org.getPlan().getTitle();
        } else if (org.getPlan() != null && org.getPlan().getPlanKey() != null) {
            planTitle = org.getPlan().getPlanKey();
        }
        String accessEnd = "—";
        if (org.getNextBillingAt() != null) {
            accessEnd = ACCESS_END_DISPLAY.format(
                    org.getNextBillingAt().toLocalDate()
            );
        }
        return new SchoolSubscriptionInfoResponse(planTitle, accessEnd);
    }

    private String formatDate(java.time.Instant instant) {
        if (instant == null) return "—";
        return DATE_FMT.format(instant.atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
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

