package com.education.web.teacher;

import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.homework.HomeworkPortalSubmissionEntity;
import com.education.web.homework.HomeworkPortalSubmissionJpaRepository;
import com.education.web.teacher.dto.TeacherHomeworkStarsChartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TeacherHomeworkStarsChartService {

    private static final int MAX_RANGE_DAYS = 800;
    /** До стільки днів — одна точка на день; інакше — по місяцях. */
    private static final int DAILY_BUCKET_MAX_DAYS = 93;

    private final TeacherJpaRepository teachers;
    private final HomeworkPortalSubmissionJpaRepository submissions;

    public TeacherHomeworkStarsChartService(
            TeacherJpaRepository teachers,
            HomeworkPortalSubmissionJpaRepository submissions
    ) {
        this.teachers = teachers;
        this.submissions = submissions;
    }

    @Transactional(readOnly = true)
    public TeacherHomeworkStarsChartResponse chart(String userId, LocalDate from, LocalDate to) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing from or to date");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        }
        long span = ChronoUnit.DAYS.between(from, to) + 1;
        if (span > MAX_RANGE_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date range too wide (max " + MAX_RANGE_DAYS + " days)"
            );
        }

        TeacherEntity teacher = teachers.findByUser_Id(userId.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher profile not found for this user"
                ));
        String teacherId = teacher.getId();

        List<HomeworkPortalSubmissionEntity> graded =
                submissions.findByTeacherIdAndStatusOrderBySubmittedAtDesc(teacherId, "graded");

        ZoneId zone = ZoneId.systemDefault();
        List<HomeworkPortalSubmissionEntity> inRange = new ArrayList<>();
        for (HomeworkPortalSubmissionEntity h : graded) {
            Instant when = h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt();
            if (when == null) {
                continue;
            }
            LocalDate d = when.atZone(zone).toLocalDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                inRange.add(h);
            }
        }

        Map<String, String> canonicalByNorm = new LinkedHashMap<>();
        for (HomeworkPortalSubmissionEntity h : inRange) {
            String norm = normalizeSubjectKey(h.getSubjectTitle());
            canonicalByNorm.putIfAbsent(norm.toLowerCase(Locale.ROOT), norm);
        }
        List<String> subjectKeys = canonicalByNorm.values().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        boolean daily = span <= DAILY_BUCKET_MAX_DAYS;
        List<String> labels;
        int bucketCount;
        if (daily) {
            labels = new ArrayList<>();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                labels.add(formatDayLabel(d));
            }
            bucketCount = labels.size();
        } else {
            labels = monthLabelsBetween(from, to);
            bucketCount = labels.size();
        }

        if (bucketCount == 0) {
            return new TeacherHomeworkStarsChartResponse(List.of(), Map.of());
        }

        int subjectCount = subjectKeys.size();
        if (subjectCount == 0) {
            return new TeacherHomeworkStarsChartResponse(labels, Map.of());
        }

        int[][] inc = new int[subjectCount][bucketCount];
        for (HomeworkPortalSubmissionEntity h : inRange) {
            String norm = normalizeSubjectKey(h.getSubjectTitle());
            String canon = canonicalByNorm.get(norm.toLowerCase(Locale.ROOT));
            if (canon == null) {
                continue;
            }
            int si = subjectKeys.indexOf(canon);
            if (si < 0) {
                continue;
            }
            Instant when = h.getGradedAt() != null ? h.getGradedAt() : h.getSubmittedAt();
            if (when == null) {
                continue;
            }
            LocalDate gradeDay = when.atZone(zone).toLocalDate();
            int bi = daily
                    ? (int) ChronoUnit.DAYS.between(from, gradeDay)
                    : monthBucketIndex(from, gradeDay, labels);
            if (bi < 0 || bi >= bucketCount) {
                continue;
            }
            int add = h.getStars() != null ? h.getStars() : 0;
            inc[si][bi] += add;
        }

        Map<String, List<Integer>> series = new LinkedHashMap<>();
        for (int si = 0; si < subjectCount; si++) {
            List<Integer> cumulative = new ArrayList<>(bucketCount);
            int run = 0;
            for (int bi = 0; bi < bucketCount; bi++) {
                run += inc[si][bi];
                cumulative.add(run);
            }
            series.put(subjectKeys.get(si), cumulative);
        }

        return new TeacherHomeworkStarsChartResponse(labels, series);
    }

    private static String normalizeSubjectKey(String subjectTitle) {
        if (subjectTitle == null || subjectTitle.isBlank()) {
            return "—";
        }
        return subjectTitle.trim();
    }

    private static String formatDayLabel(LocalDate d) {
        return d.getDayOfMonth() + " "
                + d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    private static List<String> monthLabelsBetween(LocalDate from, LocalDate to) {
        List<String> labels = new ArrayList<>();
        YearMonth cur = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);
        while (!cur.isAfter(end)) {
            labels.add(formatMonthLabel(cur));
            cur = cur.plusMonths(1);
        }
        return labels;
    }

    private static String formatMonthLabel(YearMonth ym) {
        return ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
    }

    /**
     * Індекс місячного відра для {@code gradeDay} у списку міток, побудованому від {@code rangeFrom}.
     */
    private static int monthBucketIndex(LocalDate rangeFrom, LocalDate gradeDay, List<String> labels) {
        YearMonth ymGrade = YearMonth.from(gradeDay);
        YearMonth cur = YearMonth.from(rangeFrom);
        int i = 0;
        while (i < labels.size()) {
            if (cur.equals(ymGrade)) {
                return i;
            }
            cur = cur.plusMonths(1);
            i++;
        }
        return -1;
    }
}
