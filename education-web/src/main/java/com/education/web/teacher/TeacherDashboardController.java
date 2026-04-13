package com.education.web.teacher;

import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import com.education.web.schooladmin.dto.StudentRowResponse;
import com.education.web.teacher.dto.TeacherActivityEntryResponse;
import com.education.web.teacher.dto.TeacherGroupStatsResponse;
import com.education.web.teacher.dto.TeacherHomeworkStarsChartResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;
    private final TeacherGroupStatsService teacherGroupStatsService;
    private final TeacherHomeworkStarsChartService teacherHomeworkStarsChartService;

    public TeacherDashboardController(
            TeacherDashboardService teacherDashboardService,
            TeacherGroupStatsService teacherGroupStatsService,
            TeacherHomeworkStarsChartService teacherHomeworkStarsChartService
    ) {
        this.teacherDashboardService = teacherDashboardService;
        this.teacherGroupStatsService = teacherGroupStatsService;
        this.teacherHomeworkStarsChartService = teacherHomeworkStarsChartService;
    }

    /**
     * Groups where the logged-in user is the assigned teacher ({@code school_groups.teacher_id}).
     * {@code userId} must match the authenticated user (same pattern as school-admin {@code schoolId} query).
     */
    @GetMapping("/groups")
    public List<SchoolGroupCardResponse> myGroups(@RequestParam("userId") String userId) {
        return teacherDashboardService.listGroupsForTeacherUser(userId);
    }

    /** Студенти, зараховані в групи цього вчителя. */
    @GetMapping("/students")
    public List<StudentRowResponse> myStudents(@RequestParam("userId") String userId) {
        return teacherDashboardService.listRosterForTeacherUser(userId);
    }

    /** Останні зарахування на групи (з {@code school_group_students}). */
    @GetMapping("/activity")
    public List<TeacherActivityEntryResponse> myActivity(@RequestParam("userId") String userId) {
        return teacherDashboardService.listActivityForTeacherUser(userId);
    }

    /**
     * По кожній групі вчителя: предмети з teacher_subjects (узгоджені з topics_label групи) і
     * сума зірок з оцінених здач homework portal по кожному учню та предмету.
     */
    @GetMapping("/group-stats")
    public List<TeacherGroupStatsResponse> groupStats(@RequestParam("userId") String userId) {
        return teacherGroupStatsService.listGroupStats(userId);
    }

    /**
     * Кумулятивні зірки з оцінених ДЗ цього вчителя по предметах (bucketLabels = дні або місяці).
     * Два шляхи: канонічний і короткий алиас (часто плутають з «stats»).
     */
    @GetMapping({"/homework-stars-chart", "/homework-stats"})
    public TeacherHomeworkStarsChartResponse homeworkStarsChart(
            @RequestParam("userId") String userId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return teacherHomeworkStarsChartService.chart(userId, from, to);
    }
}
