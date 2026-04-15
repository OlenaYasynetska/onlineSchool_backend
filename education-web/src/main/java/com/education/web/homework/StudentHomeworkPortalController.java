package com.education.web.homework;

import com.education.web.homework.dto.HomeworkSubmissionResponse;
import com.education.web.homework.dto.StudentDashboardContextResponse;
import com.education.web.homework.dto.StudentGroupOptionResponse;
import com.education.web.homework.dto.StudentMyStarsResponse;
import com.education.web.homework.dto.TeacherOptionShortResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

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

    /** Предмети вчителя з teacher_subjects (для випадаючого списку на формі здачі ДЗ). */
    @GetMapping("/teacher-subjects")
    public List<String> teacherSubjects(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam("teacherId") @NotBlank String teacherId
    ) {
        return service.listSubjectTitlesForTeacher(userId, teacherId);
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
    public StudentMyStarsResponse myStars(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam(value = "chartFrom", required = false)
            @DateTimeFormat(iso = ISO.DATE) LocalDate chartFrom,
            @RequestParam(value = "chartTo", required = false)
            @DateTimeFormat(iso = ISO.DATE) LocalDate chartTo
    ) {
        return service.myStars(userId, chartFrom, chartTo);
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
