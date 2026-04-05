package com.education.web.teacher;

import com.education.application.student.GetStudentsBySchoolUseCase;
import com.education.application.student.StudentView;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupStudentEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import com.education.web.schooladmin.dto.StudentRowResponse;
import com.education.web.teacher.dto.TeacherActivityEntryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeacherDashboardService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ACTIVITY_DATE_DISPLAY =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int ACTIVITY_LIMIT = 50;

    private final TeacherJpaRepository teachers;
    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolGroupStudentJpaRepository schoolGroupStudents;
    private final GetStudentsBySchoolUseCase getStudentsBySchoolUseCase;

    public TeacherDashboardService(
            TeacherJpaRepository teachers,
            SchoolGroupJpaRepository schoolGroups,
            SchoolGroupStudentJpaRepository schoolGroupStudents,
            GetStudentsBySchoolUseCase getStudentsBySchoolUseCase
    ) {
        this.teachers = teachers;
        this.schoolGroups = schoolGroups;
        this.schoolGroupStudents = schoolGroupStudents;
        this.getStudentsBySchoolUseCase = getStudentsBySchoolUseCase;
    }

    @Transactional(readOnly = true)
    public List<SchoolGroupCardResponse> listGroupsForTeacherUser(String userId) {
        TeacherEntity teacher = requireTeacher(userId);
        return schoolGroups.findByTeacher_IdOrderByNameAsc(teacher.getId()).stream()
                .map(this::toGroupCard)
                .toList();
    }

    /**
     * Студенти, зараховані хоча б в одну з груп цього вчителя (дані з БД).
     */
    @Transactional(readOnly = true)
    public List<StudentRowResponse> listRosterForTeacherUser(String userId) {
        TeacherEntity teacher = requireTeacher(userId);
        String schoolId = teacher.getSchool().getId();
        List<SchoolGroupStudentEntity> links =
                enrollmentLinksForTeacher(teacher.getId(), schoolId);
        Map<String, List<String>> groupNamesByStudent = groupNamesByStudent(links);
        if (groupNamesByStudent.isEmpty()) {
            return List.of();
        }
        Set<String> studentIds = groupNamesByStudent.keySet();
        List<StudentView> schoolStudents =
                getStudentsBySchoolUseCase.executeBySchoolId(schoolId);
        return schoolStudents.stream()
                .filter(s -> studentIds.contains(s.id()))
                .map(s -> new StudentRowResponse(
                        s.id(),
                        s.fullName(),
                        s.email(),
                        formatDate(s.createdAt()),
                        groupNamesByStudent.getOrDefault(s.id(), List.of())
                ))
                .sorted(Comparator.comparing(StudentRowResponse::fullName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Останні зарахування на групи вчителя (реальні записи {@code school_group_students}).
     */
    @Transactional(readOnly = true)
    public List<TeacherActivityEntryResponse> listActivityForTeacherUser(String userId) {
        TeacherEntity teacher = requireTeacher(userId);
        String schoolId = teacher.getSchool().getId();
        List<SchoolGroupStudentEntity> links =
                enrollmentLinksForTeacher(teacher.getId(), schoolId);
        Map<String, StudentView> byStudentId = getStudentsBySchoolUseCase
                .executeBySchoolId(schoolId)
                .stream()
                .collect(Collectors.toMap(StudentView::id, s -> s, (a, b) -> a));

        List<TeacherActivityEntryResponse> out = new ArrayList<>();
        int n = 0;
        for (SchoolGroupStudentEntity link : links) {
            if (n >= ACTIVITY_LIMIT) {
                break;
            }
            StudentView sv = byStudentId.get(link.getStudentId());
            String name = sv != null ? sv.fullName() : "—";
            String groupName = link.getGroup() != null ? link.getGroup().getName() : "—";
            Instant at = link.getCreatedAt();
            String dateStr = at != null
                    ? ACTIVITY_DATE_DISPLAY.format(at.atZone(ZoneId.systemDefault()).toLocalDate())
                    : "—";
            out.add(new TeacherActivityEntryResponse(
                    dateStr,
                    name,
                    1,
                    "Joined group " + groupName
            ));
            n++;
        }
        return out;
    }

    /**
     * Усі рядки {@code school_group_students} для груп цього вчителя.
     * Беремо зв’язки школи як у адмін-дашборді, потім лишаємо лише групи цього викладача.
     */
    private List<SchoolGroupStudentEntity> enrollmentLinksForTeacher(
            String teacherId,
            String schoolOrganizationId
    ) {
        List<SchoolGroupEntity> teacherGroups =
                schoolGroups.findByTeacher_IdOrderByNameAsc(teacherId);
        if (teacherGroups.isEmpty()) {
            return List.of();
        }
        Set<String> teacherGroupIds = teacherGroups.stream()
                .map(SchoolGroupEntity::getId)
                .collect(Collectors.toSet());
        List<SchoolGroupStudentEntity> schoolLinks =
                schoolGroupStudents.findByGroup_Organization_Id(schoolOrganizationId);
        return schoolLinks.stream()
                .filter(l -> teacherGroupIds.contains(l.getGroup().getId()))
                .sorted(Comparator.comparing(SchoolGroupStudentEntity::getCreatedAt).reversed())
                .toList();
    }

    private TeacherEntity requireTeacher(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        return teachers.findByUser_Id(userId.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher profile not found for this user"
                ));
    }

    private Map<String, List<String>> groupNamesByStudent(List<SchoolGroupStudentEntity> links) {
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

    private String formatDate(Instant instant) {
        if (instant == null) {
            return "—";
        }
        return DATE_FMT.format(instant.atZone(ZoneId.systemDefault()).toLocalDate());
    }
}
