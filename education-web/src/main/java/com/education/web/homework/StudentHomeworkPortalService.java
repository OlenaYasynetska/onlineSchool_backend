package com.education.web.homework;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupStudentEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import com.education.web.auth.model.TeacherSubjectEntity;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.TeacherSubjectJpaRepository;
import com.education.web.homework.dto.HomeworkFileDownload;
import com.education.web.homework.dto.HomeworkSubmissionResponse;
import com.education.web.homework.dto.StarRewardLogRow;
import com.education.web.homework.dto.StudentDashboardContextResponse;
import com.education.web.homework.dto.StudentGroupOptionResponse;
import com.education.web.homework.dto.StudentMyStarsResponse;
import com.education.web.homework.dto.SubjectHomeworkProgressRow;
import com.education.web.homework.dto.SubjectStarTotalRow;
import com.education.web.homework.dto.TeacherOptionShortResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentHomeworkPortalService {

    private static final Logger log = LoggerFactory.getLogger(StudentHomeworkPortalService.class);

    /**
     * Як {@link com.education.web.teacher.TeacherHomeworkStarsChartService}: до стільки днів у вікні —
     * одна точка на день; інакше — по місяцях.
     */
    private static final int CHART_DAILY_MAX_SPAN_DAYS = 93;

    private final SpringDataStudentJpaRepository students;
    private final TeacherJpaRepository teachers;
    private final SchoolGroupStudentJpaRepository groupStudents;
    private final SchoolGroupJpaRepository schoolGroups;
    private final HomeworkPortalSubmissionJpaRepository submissions;
    private final OrganizationJpaRepository organizations;
    private final TeacherSubjectJpaRepository teacherSubjects;

    private final HomeworkSubmissionFileLoader fileLoader;

    public StudentHomeworkPortalService(
            SpringDataStudentJpaRepository students,
            TeacherJpaRepository teachers,
            SchoolGroupStudentJpaRepository groupStudents,
            SchoolGroupJpaRepository schoolGroups,
            HomeworkPortalSubmissionJpaRepository submissions,
            OrganizationJpaRepository organizations,
            TeacherSubjectJpaRepository teacherSubjects,
            HomeworkSubmissionFileLoader fileLoader
    ) {
        this.students = students;
        this.teachers = teachers;
        this.groupStudents = groupStudents;
        this.schoolGroups = schoolGroups;
        this.submissions = submissions;
        this.organizations = organizations;
        this.teacherSubjects = teacherSubjects;
        this.fileLoader = fileLoader;
    }

    /** Завантаження / перегляд власного вкладення учнем (лише своя здача). */
    public HomeworkFileDownload getStudentOwnFileDownload(String userId, String submissionId) {
        StudentJpaEntity student = requireStudentByUser(userId);
        HomeworkPortalSubmissionEntity s = submissions.findById(submissionId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")
        );
        if (!student.getId().equals(s.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your submission");
        }
        return fileLoader.loadForSubmission(s);
    }

    /** Назва школи та зараховані групи — з БД. */
    @Transactional(readOnly = true)
    public StudentDashboardContextResponse dashboardContext(String userId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        String schoolName = organizations.findById(st.getSchoolId())
                .map(OrganizationEntity::getName)
                .orElse("—");
        List<StudentGroupOptionResponse> groups = groupStudents.findByStudentIdFetchGroup(st.getId()).stream()
                .map(SchoolGroupStudentEntity::getGroup)
                .map(g -> new StudentGroupOptionResponse(g.getId(), g.getName(), g.getCode()))
                .collect(Collectors.toList());
        return new StudentDashboardContextResponse(schoolName, groups);
    }

    public List<TeacherOptionShortResponse> listTeachersForStudent(String userId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        return teachers.findAllBySchoolIdWithUserOrderByName(st.getSchoolId()).stream()
                .map(t -> {
                    var u = t.getUser();
                    String dn = (u.getFirstName() + " " + u.getLastName()).trim();
                    return new TeacherOptionShortResponse(t.getId(), dn);
                })
                .collect(Collectors.toList());
    }

    /**
     * Предмети з {@code teacher_subjects} для обраного вчителя (та сама школа, що й у учня).
     */
    @Transactional(readOnly = true)
    public List<String> listSubjectTitlesForTeacher(String userId, String teacherId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        if (teacherId == null || teacherId.isBlank()) {
            return List.of();
        }
        TeacherEntity teacher = teachers
                .findByIdAndSchool_Id(teacherId.trim(), st.getSchoolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));
        return teacherSubjects.findByTeacher_IdOrderBySortOrderAsc(teacher.getId()).stream()
                .map(TeacherSubjectEntity::getTitle)
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    /** Групи учня з БД — зв’язок {@code school_group_students}, не через учителя. */
    @Transactional(readOnly = true)
    public List<StudentGroupOptionResponse> listGroupsForStudent(String userId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        return groupStudents.findByStudentIdFetchGroup(st.getId()).stream()
                .map(SchoolGroupStudentEntity::getGroup)
                .map(g -> new StudentGroupOptionResponse(g.getId(), g.getName(), g.getCode()))
                .collect(Collectors.toList());
    }

    public List<HomeworkSubmissionResponse> listMySubmissions(String userId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        return submissions.findByStudentIdOrderBySubmittedAtDesc(st.getId()).stream()
                .map(s -> toResponse(s, st.getFullName(), st.getEmail()))
                .collect(Collectors.toList());
    }

    /** Зірки та журнал з оцінених ДЗ (homework_portal_submissions) для графіка й таблиць. */
    @Transactional(readOnly = true)
    public StudentMyStarsResponse myStars(String userId, LocalDate chartFromOpt, LocalDate chartToOpt) {
        StudentJpaEntity st = requireStudentByUser(userId);
        List<HomeworkPortalSubmissionEntity> allSubs =
                submissions.findByStudentIdOrderBySubmittedAtDesc(st.getId());
        List<HomeworkPortalSubmissionEntity> graded = allSubs.stream()
                .filter(s -> "graded".equals(s.getStatus()))
                .toList();

        Map<String, int[]> progressAgg = new LinkedHashMap<>();
        for (HomeworkPortalSubmissionEntity h : allSubs) {
            String subj = normalizeSubjectKey(h.getSubjectTitle());
            int[] a = progressAgg.computeIfAbsent(subj, k -> new int[3]);
            a[0]++;
            if ("graded".equals(h.getStatus())) {
                a[1]++;
                a[2] += h.getStars() != null ? h.getStars() : 0;
            }
        }
        List<SubjectHomeworkProgressRow> subjectHomeworkProgress = progressAgg.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue()[0], e1.getValue()[0]))
                .map(e -> new SubjectHomeworkProgressRow(
                        e.getKey(),
                        e.getValue()[0],
                        e.getValue()[1],
                        e.getValue()[2]))
                .toList();

        int totalStars = graded.stream()
                .mapToInt(h -> h.getStars() != null ? h.getStars() : 0)
                .sum();

        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
        Instant monthAgo = now.minus(30, ChronoUnit.DAYS);
        int weekGain = 0;
        int monthGain = 0;
        for (HomeworkPortalSubmissionEntity h : graded) {
            Instant g = h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt();
            int add = h.getStars() != null ? h.getStars() : 0;
            if (g.isAfter(weekAgo)) {
                weekGain += add;
            }
            if (g.isAfter(monthAgo)) {
                monthGain += add;
            }
        }

        Map<String, Integer> totalsMap = new LinkedHashMap<>();
        for (HomeworkPortalSubmissionEntity h : graded) {
            String subj = normalizeSubjectKey(h.getSubjectTitle());
            int add = h.getStars() != null ? h.getStars() : 0;
            totalsMap.merge(subj, add, Integer::sum);
        }
        List<SubjectStarTotalRow> subjectTotals = totalsMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(e -> new SubjectStarTotalRow(e.getKey(), e.getValue()))
                .toList();

        ZoneId zone = ZoneId.systemDefault();
        LocalDate todayLocal = LocalDate.now(zone);
        LocalDate rangeFrom = chartFromOpt != null ? chartFromOpt : todayLocal.withDayOfMonth(1);
        LocalDate rangeTo = chartToOpt != null ? chartToOpt : todayLocal;
        final LocalDate chartFrom;
        final LocalDate chartTo;
        if (rangeFrom.isAfter(rangeTo)) {
            chartFrom = rangeTo;
            chartTo = rangeFrom;
        } else {
            chartFrom = rangeFrom;
            chartTo = rangeTo;
        }

        long spanDays = ChronoUnit.DAYS.between(chartFrom, chartTo) + 1;
        boolean useDaily = spanDays <= CHART_DAILY_MAX_SPAN_DAYS;

        List<HomeworkPortalSubmissionEntity> gradedInRange = graded.stream()
                .filter(h -> {
                    Instant when = h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt();
                    if (when == null) {
                        return false;
                    }
                    LocalDate d = when.atZone(zone).toLocalDate();
                    return !d.isBefore(chartFrom) && !d.isAfter(chartTo);
                })
                .toList();

        Map<String, Integer> totalsInWindow = new LinkedHashMap<>();
        for (HomeworkPortalSubmissionEntity h : gradedInRange) {
            String subj = normalizeSubjectKey(h.getSubjectTitle());
            int add = h.getStars() != null ? h.getStars() : 0;
            totalsInWindow.merge(subj, add, Integer::sum);
        }
        List<String> subjectKeys = totalsInWindow.keySet().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<String> chartLabels;
        Map<String, List<Integer>> chartSeries;
        String chartGranularity;
        if (subjectKeys.isEmpty()) {
            chartGranularity = useDaily ? "DAY" : "MONTH";
            if (useDaily) {
                List<LocalDate> days = new ArrayList<>();
                for (LocalDate d = chartFrom; !d.isAfter(chartTo); d = d.plusDays(1)) {
                    days.add(d);
                }
                chartLabels = days.stream().map(StudentHomeworkPortalService::formatDayLabel).toList();
            } else {
                List<YearMonth> months = new ArrayList<>();
                YearMonth startYm = YearMonth.from(chartFrom);
                YearMonth endYm = YearMonth.from(chartTo);
                for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
                    months.add(ym);
                }
                chartLabels = months.stream().map(this::formatMonthLabel).toList();
            }
            chartSeries = new LinkedHashMap<>();
        } else if (useDaily) {
            chartGranularity = "DAY";
            List<LocalDate> days = new ArrayList<>();
            for (LocalDate d = chartFrom; !d.isAfter(chartTo); d = d.plusDays(1)) {
                days.add(d);
            }
            chartLabels = days.stream().map(StudentHomeworkPortalService::formatDayLabel).toList();
            int[][] raw = new int[subjectKeys.size()][days.size()];
            for (HomeworkPortalSubmissionEntity h : gradedInRange) {
                String subj = normalizeSubjectKey(h.getSubjectTitle());
                int si = subjectKeys.indexOf(subj);
                if (si < 0) {
                    continue;
                }
                Instant when = h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt();
                LocalDate d = when.atZone(zone).toLocalDate();
                int di = days.indexOf(d);
                if (di < 0) {
                    continue;
                }
                int add = h.getStars() != null ? h.getStars() : 0;
                raw[si][di] += add;
            }
            chartSeries = buildCumulativeSeries(subjectKeys, raw);
        } else {
            chartGranularity = "MONTH";
            List<YearMonth> months = new ArrayList<>();
            YearMonth startYm = YearMonth.from(chartFrom);
            YearMonth endYm = YearMonth.from(chartTo);
            for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
                months.add(ym);
            }
            chartLabels = months.stream().map(this::formatMonthLabel).toList();
            int[][] raw = new int[subjectKeys.size()][months.size()];
            for (HomeworkPortalSubmissionEntity h : gradedInRange) {
                String subj = normalizeSubjectKey(h.getSubjectTitle());
                int si = subjectKeys.indexOf(subj);
                if (si < 0) {
                    continue;
                }
                Instant when = h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt();
                YearMonth ym = YearMonth.from(when.atZone(zone));
                int mi = months.indexOf(ym);
                if (mi < 0) {
                    continue;
                }
                int add = h.getStars() != null ? h.getStars() : 0;
                raw[si][mi] += add;
            }
            chartSeries = buildCumulativeSeries(subjectKeys, raw);
        }

        List<StarRewardLogRow> rewardLog = graded.stream()
                .sorted(Comparator.comparing((HomeworkPortalSubmissionEntity h) ->
                        h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt()).reversed())
                .map(h -> {
                    TeacherEntity t = teachers.findById(h.getTeacherId()).orElse(null);
                    String tn = "—";
                    if (t != null && t.getUser() != null) {
                        tn = (t.getUser().getFirstName() + " " + t.getUser().getLastName()).trim();
                        if (tn.isEmpty()) {
                            tn = "—";
                        }
                    }
                    String subj = normalizeSubjectKey(h.getSubjectTitle());
                    return new StarRewardLogRow(
                            h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt(),
                            tn,
                            subj,
                            h.getStars() != null ? h.getStars() : 0,
                            h.getTeacherFeedback()
                    );
                })
                .toList();

        return new StudentMyStarsResponse(
                totalStars,
                weekGain,
                monthGain,
                subjectTotals,
                chartLabels,
                chartSeries,
                rewardLog,
                subjectHomeworkProgress,
                chartGranularity
        );
    }

    private static Map<String, List<Integer>> buildCumulativeSeries(List<String> subjectKeys, int[][] raw) {
        Map<String, List<Integer>> chartSeries = new LinkedHashMap<>();
        int bucketCount = subjectKeys.isEmpty() || raw.length == 0 ? 0 : raw[0].length;
        for (int si = 0; si < subjectKeys.size(); si++) {
            List<Integer> cumulative = new ArrayList<>();
            int run = 0;
            for (int bi = 0; bi < bucketCount; bi++) {
                run += raw[si][bi];
                cumulative.add(run);
            }
            chartSeries.put(subjectKeys.get(si), cumulative);
        }
        return chartSeries;
    }

    private static String normalizeSubjectKey(String subjectTitle) {
        if (subjectTitle == null || subjectTitle.isBlank()) {
            return "—";
        }
        return subjectTitle.trim();
    }

    private String formatMonthLabel(YearMonth ym) {
        return ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
    }

    /** Як у {@link com.education.web.teacher.TeacherHomeworkStarsChartService} — підпис дня на осі X. */
    private static String formatDayLabel(LocalDate d) {
        return d.getDayOfMonth() + " "
                + d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    @Transactional
    public HomeworkSubmissionResponse submit(
            String userId,
            String teacherId,
            String groupId,
            String subjectTitle,
            String messageText,
            MultipartFile file
    ) {
        boolean hasFile = file != null && !file.isEmpty() && !isNoAttachmentPlaceholder(file);
        StudentJpaEntity student = requireStudentByUser(userId);
        TeacherEntity teacher = teachers.findById(teacherId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found")
        );
        if (!teacher.getSchool().getId().equals(student.getSchoolId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher is not from your school");
        }
        var memberships = groupStudents.findByStudentIdFetchGroup(student.getId());

        String gid = groupId != null ? groupId.trim() : "";
        String groupIdToStore = null;
        if (!gid.isBlank()) {
            if (groupStudents.existsByStudentIdAndGroup_Id(student.getId(), gid)) {
                groupIdToStore = gid;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not enrolled in this group");
            }
        } else if (memberships.size() == 1) {
            groupIdToStore = memberships.get(0).getGroup().getId();
        } else if (memberships.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Select a group for this homework (you are enrolled in multiple groups)"
            );
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You are not enrolled in any class group. Ask your school administrator to add you under Groups in the school admin panel."
            );
        }
        String subj = subjectTitle != null ? subjectTitle.trim() : "";
        if (subj.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required");
        }
        List<TeacherSubjectEntity> assignedSubjects =
                teacherSubjects.findByTeacher_IdOrderBySortOrderAsc(teacher.getId());
        if (!assignedSubjects.isEmpty()) {
            boolean allowed = assignedSubjects.stream()
                    .map(TeacherSubjectEntity::getTitle)
                    .filter(t -> t != null && !t.isBlank())
                    .anyMatch(t -> t.trim().equalsIgnoreCase(subj));
            if (!allowed) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Subject must be one of this teacher's subjects"
                );
            }
        }

        String submissionId = UUID.randomUUID().toString();
        String orig;
        String relativePath;
        if (hasFile) {
            orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String safe = safeFileName(orig);
            String storedFileName = UUID.randomUUID() + "_" + safe;
            relativePath = submissionId + "/" + storedFileName;
        } else {
            orig = "(no file)";
            relativePath = submissionId + "/.no-file.txt";
        }

        HomeworkPortalSubmissionEntity row = new HomeworkPortalSubmissionEntity();
        row.setId(submissionId);
        row.setStudentId(student.getId());
        row.setTeacherId(teacher.getId());
        row.setGroupId(groupIdToStore);
        row.setSubjectTitle(subj);
        row.setMessageText(messageText != null ? messageText.trim() : null);
        row.setFileName(orig);
        row.setStoragePath(relativePath);
        if (hasFile) {
            row.setContentType(file.getContentType());
            row.setFileSizeBytes(file.getSize());
        } else {
            row.setContentType(null);
            row.setFileSizeBytes(0L);
        }
        row.setStatus("submitted");
        row.setSubmittedAt(Instant.now());

        Path uploadRoot = fileLoader.getUploadRoot();
        Path target = uploadRoot.resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            if (hasFile) {
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.write(target, new byte[0]);
            }
        } catch (IOException e) {
            log.error(
                    "Homework upload failed (dir={}): {}",
                    uploadRoot,
                    e.toString(),
                    e
            );
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save file. "
                            + "Set HOMEWORK_UPLOAD_DIR to a writable folder (e.g. /tmp/homework or a mounted volume). "
                            + "Current path: "
                            + uploadRoot
                            + ". "
                            + e.getMessage()
            );
        }

        submissions.save(row);
        return toResponse(row, student.getFullName(), student.getEmail());
    }

    private StudentJpaEntity requireStudentByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        return students.findByUserId(userId.trim()).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not linked to this account (user_id in students)"
                )
        );
    }

    private HomeworkSubmissionResponse toResponse(
            HomeworkPortalSubmissionEntity s,
            String studentName,
            String studentEmail
    ) {
        String groupName = null;
        if (s.getGroupId() != null) {
            groupName = schoolGroups.findById(s.getGroupId()).map(SchoolGroupEntity::getName).orElse(null);
        }
        return new HomeworkSubmissionResponse(
                s.getId(),
                studentName,
                studentEmail,
                s.getSubjectTitle(),
                s.getMessageText(),
                s.getFileName(),
                s.getStatus(),
                s.getStars(),
                s.getTeacherFeedback(),
                groupName,
                s.getSubmittedAt(),
                s.getGradedAt()
        );
    }

    /** Placeholder uploads meaning «submit without attachment» (see frontend FormData). */
    private static boolean isNoAttachmentPlaceholder(MultipartFile file) {
        String n = file.getOriginalFilename();
        if (n == null) {
            return false;
        }
        String t = n.trim();
        return "no-attachment.txt".equalsIgnoreCase(t)
                || "__no_hw_attachment__.txt".equalsIgnoreCase(t);
    }

    private static String safeFileName(String name) {
        String n = name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        if (n.isBlank()) {
            n = "file";
        }
        if (n.length() > 200) {
            n = n.substring(0, 200);
        }
        return n;
    }

}
