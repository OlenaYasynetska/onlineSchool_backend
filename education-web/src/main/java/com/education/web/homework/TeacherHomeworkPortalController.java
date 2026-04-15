package com.education.web.homework;

import com.education.web.homework.dto.GradeHomeworkRequest;
import com.education.web.homework.dto.HomeworkSubmissionResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/homework")
public class TeacherHomeworkPortalController {

    private final TeacherHomeworkPortalService service;

    public TeacherHomeworkPortalController(TeacherHomeworkPortalService service) {
        this.service = service;
    }

    @GetMapping("/pending")
    public List<HomeworkSubmissionResponse> pending(@RequestParam("userId") String userId) {
        return service.listPending(userId);
    }

    @GetMapping("/graded")
    public List<HomeworkSubmissionResponse> graded(@RequestParam("userId") String userId) {
        return service.listMyGraded(userId);
    }

    @PostMapping("/{submissionId}/grade")
    public HomeworkSubmissionResponse grade(
            @RequestParam("userId") String userId,
            @PathVariable("submissionId") String submissionId,
            @Valid @RequestBody GradeHomeworkRequest body
    ) {
        return service.grade(userId, submissionId, body);
    }

    /**
     * Видача файлу здачі. {@code inline=true} — для перегляду в браузері (PDF/зображення);
     * за замовчуванням — завантаження (attachment).
     */
    @GetMapping("/{submissionId}/file")
    public ResponseEntity<Resource> download(
            @RequestParam("userId") String userId,
            @PathVariable("submissionId") String submissionId,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline
    ) {
        return HomeworkFileHttpResponses.toResponse(service.getFileDownload(userId, submissionId), inline);
    }
}
