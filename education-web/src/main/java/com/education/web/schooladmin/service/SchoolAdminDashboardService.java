package com.education.web.schooladmin.service;

import com.education.application.student.GetStudentsBySchoolUseCase;
import com.education.application.student.StudentView;
import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.PaymentEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.PaymentJpaRepository;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
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
import java.util.List;
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

    public SchoolAdminDashboardService(
            GetStudentsBySchoolUseCase getStudentsBySchoolUseCase,
            PaymentJpaRepository payments,
            OrganizationJpaRepository organizations,
            SchoolGroupJpaRepository schoolGroups
    ) {
        this.getStudentsBySchoolUseCase = getStudentsBySchoolUseCase;
        this.payments = payments;
        this.organizations = organizations;
        this.schoolGroups = schoolGroups;
    }

    @Transactional(readOnly = true)
    public SchoolAdminDashboardResponse getDashboard(String schoolId) {
        List<StudentView> studentViews = getStudentsBySchoolUseCase
                .executeBySchoolId(schoolId);
        List<PaymentEntity> paymentRows = payments
                .findAllByOrganization_IdOrderByCreatedAtDesc(schoolId);

        List<StudentRowResponse> studentsResponse = studentViews.stream()
                .map(s -> new StudentRowResponse(
                        s.id(),
                        s.fullName(),
                        s.email(),
                        formatDate(s.createdAt())
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

        List<SchoolGroupCardResponse> groupsResponse = schoolGroups
                .findByOrganization_IdOrderByCreatedAtDesc(schoolId)
                .stream()
                .map(this::toGroupCard)
                .toList();

        return new SchoolAdminDashboardResponse(
                new SchoolAdminDashboardStatsResponse(
                        studentViews.size(),
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

    private SchoolGroupCardResponse toGroupCard(SchoolGroupEntity g) {
        LocalDate start = g.getStartDate();
        LocalDate end = g.getEndDate();
        String topics = g.getTopicsLabel() != null ? g.getTopicsLabel() : "";
        String subjectId = g.getSubject() != null ? g.getSubject().getId() : null;
        String teacherId = g.getTeacher() != null ? g.getTeacher().getId() : null;
        return new SchoolGroupCardResponse(
                g.getId(),
                g.getName(),
                g.getCode(),
                subjectId,
                teacherId,
                topics,
                start != null ? DATE_FMT.format(start) : "—",
                end != null ? DATE_FMT.format(end) : "—",
                g.getStudentsCount(),
                g.isActive()
        );
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

