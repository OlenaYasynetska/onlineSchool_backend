package com.education.web.homework;

import com.education.web.homework.dto.HomeworkSubmissionResponse;
import com.education.web.homework.dto.StudentDashboardContextResponse;
import com.education.web.homework.dto.StudentGroupOptionResponse;
import com.education.web.homework.dto.StudentMyStarsResponse;
import com.education.web.homework.dto.TeacherOptionShortResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student/homework")
public class StudentHomeworkPortalController {

    private final StudentHomeworkPortalService service;

    public StudentHomeworkPortalController(StudentHomeworkPortalService service) {
        this.service = service;
    }

    @GetMapping("/teachers")
    public List<TeacherOptionShortResponse> teachers(@RequestParam("userId") @NotBlank String userId) {
        return service.listTeachersForStudent(userId);
    }

    @GetMapping("/groups")
    public List<StudentGroupOptionResponse> groups(@RequestParam("userId") @NotBlank String userId) {
        return service.listGroupsForStudent(userId);
    }

    /** Школа та групи учня (організація + school_group_students). */
    @GetMapping("/dashboard-context")
    public StudentDashboardContextResponse dashboardContext(@RequestParam("userId") @NotBlank String userId) {
        return service.dashboardContext(userId);
    }

    @GetMapping("/submissions")
    public List<HomeworkSubmissionResponse> mySubmissions(@RequestParam("userId") @NotBlank String userId) {
        return service.listMySubmissions(userId);
    }

    /** Зірки з оцінених ДЗ для таблиць і графіка на дашборді учня. */
    @GetMapping("/my-stars")
    public StudentMyStarsResponse myStars(@RequestParam("userId") @NotBlank String userId) {
        return service.myStars(userId);
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HomeworkSubmissionResponse submit(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam("teacherId") @NotBlank String teacherId,
            @RequestParam(value = "groupId", required = false) String groupId,
            @RequestParam("subjectTitle") @NotBlank String subjectTitle,
            @RequestParam(value = "messageText", required = false) String messageText,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        return service.submit(userId, teacherId, groupId, subjectTitle, messageText, file);
    }
}
