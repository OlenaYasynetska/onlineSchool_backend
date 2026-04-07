package com.education.web.teacher;

import com.education.application.student.GetStudentsBySchoolUseCase;
import com.education.application.student.StudentView;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupStudentEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.TeacherSubjectEntity;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.TeacherSubjectJpaRepository;
import com.education.web.homework.HomeworkPortalSubmissionEntity;
import com.education.web.homework.HomeworkPortalSubmissionJpaRepository;
import com.education.web.teacher.dto.TeacherGroupStatsResponse;
import com.education.web.teacher.dto.TeacherGroupStudentStatRow;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherGroupStatsService {

    private final TeacherJpaRepository teachers;
    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolGroupStudentJpaRepository groupStudents;
    private final TeacherSubjectJpaRepository teacherSubjects;
    private final HomeworkPortalSubmissionJpaRepository submissions;
    private final GetStudentsBySchoolUseCase getStudentsBySchoolUseCase;

    public TeacherGroupStatsService(
            TeacherJpaRepository teachers,
            SchoolGroupJpaRepository schoolGroups,
            SchoolGroupStudentJpaRepository groupStudents,
            TeacherSubjectJpaRepository teacherSubjects,
            HomeworkPortalSubmissionJpaRepository submissions,
            GetStudentsBySchoolUseCase getStudentsBySchoolUseCase
    ) {
        this.teachers = teachers;
        this.schoolGroups = schoolGroups;
        this.groupStudents = groupStudents;
        this.teacherSubjects = teacherSubjects;
        this.submissions = submissions;
        this.getStudentsBySchoolUseCase = getStudentsBySchoolUseCase;
    }

    @Transactional(readOnly = true)
    public List<TeacherGroupStatsResponse> listGroupStats(String userId) {
        TeacherEntity teacher = requireTeacher(userId);
        String teacherId = teacher.getId();
        String schoolId = teacher.getSchool().getId();

        List<TeacherSubjectEntity> allSubjectRows =
                teacherSubjects.findByTeacher_IdOrderBySortOrderAsc(teacherId);
        List<String> allSubjectTitles = allSubjectRows.stream()
                .map(TeacherSubjectEntity::getTitle)
                .filter(t -> t != null && !t.isBlank())
                .toList();

        Map<String, StudentView> studentsById = getStudentsBySchoolUseCase
                .executeBySchoolId(schoolId)
                .stream()
                .collect(Collectors.toMap(StudentView::id, s -> s, (a, b) -> a));

        List<SchoolGroupEntity> groups = schoolGroups.findByTeacher_IdOrderByNameAsc(teacherId);
        List<TeacherGroupStatsResponse> out = new ArrayList<>();

        for (SchoolGroupEntity g : groups) {
            List<String> subjectTitles = resolveSubjectTitlesForGroup(g, allSubjectTitles);
            if (subjectTitles.isEmpty()) {
                subjectTitles = splitTopicsFallback(g.getTopicsLabel());
            }

            List<SchoolGroupStudentEntity> links =
                    groupStudents.findByGroup_IdOrderByStudentIdAsc(g.getId());

            Map<String, Map<String, Integer>> stars = new LinkedHashMap<>();
            for (SchoolGroupStudentEntity link : links) {
                String sid = link.getStudentId();
                Map<String, Integer> row = new LinkedHashMap<>();
                for (String sub : subjectTitles) {
                    row.put(sub, 0);
                }
                stars.put(sid, row);
            }

            List<HomeworkPortalSubmissionEntity> graded =
                    submissions.findByTeacherIdAndGroupIdAndStatus(teacherId, g.getId(), "graded");
            for (HomeworkPortalSubmissionEntity h : graded) {
                String sid = h.getStudentId();
                Map<String, Integer> row = stars.get(sid);
                if (row == null) {
                    continue;
                }
                String matched = matchSubjectKey(h.getSubjectTitle(), subjectTitles);
                if (matched == null) {
                    continue;
                }
                int add = h.getStars() != null ? h.getStars() : 0;
                row.merge(matched, add, Integer::sum);
            }

            List<TeacherGroupStudentStatRow> studentRows = new ArrayList<>();
            for (SchoolGroupStudentEntity link : links) {
                String sid = link.getStudentId();
                StudentView sv = studentsById.get(sid);
                String name = sv != null ? sv.fullName() : "—";
                Map<String, Integer> row = stars.getOrDefault(sid, Map.of());
                studentRows.add(new TeacherGroupStudentStatRow(sid, name, new LinkedHashMap<>(row)));
            }

            out.add(new TeacherGroupStatsResponse(
                    g.getId(),
                    g.getName(),
                    g.getCode(),
                    List.copyOf(subjectTitles),
                    studentRows
            ));
        }

        return out;
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

    /**
     * Предмети вчителя, релевантні для групи: перетин із topics_label (HTML, CSS, …),
     * інакше всі предмети з teacher_subjects.
     */
    private List<String> resolveSubjectTitlesForGroup(
            SchoolGroupEntity group,
            List<String> allSubjectTitles
    ) {
        if (allSubjectTitles.isEmpty()) {
            return splitTopicsFallback(group.getTopicsLabel());
        }
        String topics = group.getTopicsLabel();
        if (topics == null || topics.isBlank()) {
            return new ArrayList<>(allSubjectTitles);
        }
        List<String> topicTokens = Arrays.stream(topics.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        List<String> matched = new ArrayList<>();
        for (String subj : allSubjectTitles) {
            String sl = subj.toLowerCase(Locale.ROOT);
            boolean ok = topicTokens.stream().anyMatch(
                    t -> t.equalsIgnoreCase(subj)
                            || sl.contains(t.toLowerCase(Locale.ROOT))
                            || t.toLowerCase(Locale.ROOT).contains(sl)
            );
            if (ok) {
                matched.add(subj);
            }
        }
        if (!matched.isEmpty()) {
            return matched;
        }
        return new ArrayList<>(allSubjectTitles);
    }

    private List<String> splitTopicsFallback(String topicsLabel) {
        if (topicsLabel == null || topicsLabel.isBlank()) {
            return List.of();
        }
        return Arrays.stream(topicsLabel.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String matchSubjectKey(String submissionTitle, List<String> subjectTitles) {
        if (submissionTitle == null || submissionTitle.isBlank()) {
            return null;
        }
        String s = submissionTitle.trim();
        for (String sub : subjectTitles) {
            if (sub.equalsIgnoreCase(s)) {
                return sub;
            }
        }
        String sl = s.toLowerCase(Locale.ROOT);
        for (String sub : subjectTitles) {
            String tl = sub.toLowerCase(Locale.ROOT);
            if (sl.contains(tl) || tl.contains(sl)) {
                return sub;
            }
        }
        return null;
    }
}
