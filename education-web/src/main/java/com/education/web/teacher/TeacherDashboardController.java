package com.education.web.teacher;

import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;

    public TeacherDashboardController(TeacherDashboardService teacherDashboardService) {
        this.teacherDashboardService = teacherDashboardService;
    }

    /**
     * Groups where the logged-in user is the assigned teacher ({@code school_groups.teacher_id}).
     * {@code userId} must match the authenticated user (same pattern as school-admin {@code schoolId} query).
     */
    @GetMapping("/groups")
    public List<SchoolGroupCardResponse> myGroups(@RequestParam("userId") String userId) {
        return teacherDashboardService.listGroupsForTeacherUser(userId);
    }
}
