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

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeacherGroupStatsService {

    private static final int CHART_MONTHS = 6;

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

        List<HomeworkPortalSubmissionEntity> allGraded =
                submissions.findByTeacherIdAndStatusOrderBySubmittedAtDesc(teacherId, "graded");

        List<YearMonth> monthWindow = lastNMonths(CHART_MONTHS);
        List<String> monthLabels = monthWindow.stream().map(this::formatMonthLabel).toList();

        List<SchoolGroupEntity> groups = schoolGroups.findByTeacher_IdOrderByNameAsc(teacherId);
        List<TeacherGroupStatsResponse> out = new ArrayList<>();

        for (SchoolGroupEntity g : groups) {
            List<String> subjectTitles = resolveSubjectTitlesForGroup(g, allSubjectTitles);
            if (subjectTitles.isEmpty()) {
                subjectTitles = splitTopicsFallback(g.getTopicsLabel());
            }

            List<SchoolGroupStudentEntity> links =
                    groupStudents.findByGroup_IdOrderByStudentIdAsc(g.getId());

            Set<String> studentIdsInG = links.stream()
                    .map(SchoolGroupStudentEntity::getStudentId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            List<HomeworkPortalSubmissionEntity> gradedForGroup = allGraded.stream()
                    .filter(h -> appliesToGroup(h, g, teacherId, studentIdsInG))
                    .toList();

            Map<String, Map<String, Integer>> stars = new LinkedHashMap<>();
            for (SchoolGroupStudentEntity link : links) {
                String sid = link.getStudentId();
                Map<String, Integer> row = new LinkedHashMap<>();
                for (String sub : subjectTitles) {
                    row.put(sub, 0);
                }
                stars.put(sid, row);
            }

            for (HomeworkPortalSubmissionEntity h : gradedForGroup) {
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

            Map<String, List<Integer>> chartSeries = buildChartSeries(
                    subjectTitles,
                    gradedForGroup,
                    monthWindow
            );

            out.add(new TeacherGroupStatsResponse(
                    g.getId(),
                    g.getName(),
                    g.getCode(),
                    List.copyOf(subjectTitles),
                    studentRows,
                    monthLabels,
                    chartSeries
            ));
        }

        return out;
    }

    /** Учёт строки ДЗ для групи: зачисление в группу + либо group_id в строке, либо одна группа у этого учителя. */
    private boolean appliesToGroup(
            HomeworkPortalSubmissionEntity h,
            SchoolGroupEntity g,
            String teacherId,
            Set<String> studentIdsInG
    ) {
        if (!studentIdsInG.contains(h.getStudentId())) {
            return false;
        }
        String gid = h.getGroupId();
        if (gid != null && !gid.isBlank()) {
            return g.getId().equals(gid);
        }
        Set<String> tg = teacherGroupIdsForStudent(h.getStudentId(), teacherId);
        return tg.size() == 1 && tg.contains(g.getId());
    }

    private Set<String> teacherGroupIdsForStudent(String studentId, String teacherId) {
        return groupStudents.findByStudentId(studentId).stream()
                .map(SchoolGroupStudentEntity::getGroup)
                .filter(gr -> teacherId.equals(gr.getTeacher().getId()))
                .map(SchoolGroupEntity::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<YearMonth> lastNMonths(int n) {
        YearMonth now = YearMonth.now(ZoneOffset.UTC);
        List<YearMonth> list = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            list.add(now.minusMonths(i));
        }
        return list;
    }

    private String formatMonthLabel(YearMonth ym) {
        return ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
    }

    /**
     * Кумулятивні зірки по місяцях з БД ({@code graded_at}), по кожному предмету з таблиці.
     */
    private Map<String, List<Integer>> buildChartSeries(
            List<String> subjectTitles,
            List<HomeworkPortalSubmissionEntity> gradedForGroup,
            List<YearMonth> monthWindow
    ) {
        int m = monthWindow.size();
        int[][] raw = new int[subjectTitles.size()][m];
        for (HomeworkPortalSubmissionEntity h : gradedForGroup) {
            String matched = matchSubjectKey(h.getSubjectTitle(), subjectTitles);
            if (matched == null) {
                continue;
            }
            int subIdx = subjectTitles.indexOf(matched);
            if (subIdx < 0) {
                continue;
            }
            Instant when = h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt();
            YearMonth ym = YearMonth.from(when.atZone(ZoneOffset.UTC));
            int mi = monthWindow.indexOf(ym);
            if (mi < 0) {
                continue;
            }
            int add = h.getStars() != null ? h.getStars() : 0;
            raw[subIdx][mi] += add;
        }
        Map<String, List<Integer>> chartSeries = new LinkedHashMap<>();
        for (int si = 0; si < subjectTitles.size(); si++) {
            String sub = subjectTitles.get(si);
            List<Integer> cumulative = new ArrayList<>();
            int run = 0;
            for (int mi = 0; mi < m; mi++) {
                run += raw[si][mi];
                cumulative.add(run);
            }
            chartSeries.put(sub, cumulative);
        }
        return chartSeries;
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
