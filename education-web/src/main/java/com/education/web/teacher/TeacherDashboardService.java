package com.education.web.teacher;

import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TeacherDashboardService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TeacherJpaRepository teachers;
    private final SchoolGroupJpaRepository schoolGroups;

    public TeacherDashboardService(
            TeacherJpaRepository teachers,
            SchoolGroupJpaRepository schoolGroups
    ) {
        this.teachers = teachers;
        this.schoolGroups = schoolGroups;
    }

    @Transactional(readOnly = true)
    public List<SchoolGroupCardResponse> listGroupsForTeacherUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        TeacherEntity teacher = teachers.findByUser_Id(userId.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher profile not found for this user"
                ));
        return schoolGroups.findByTeacher_IdOrderByNameAsc(teacher.getId()).stream()
                .map(this::toGroupCard)
                .toList();
    }

    private SchoolGroupCardResponse toGroupCard(SchoolGroupEntity g) {
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
                g.getStudentsCount(),
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
}
