package com.education.web.materials;

import com.education.web.materials.dto.StudyMaterialLessonResponse;
import com.education.web.materials.dto.StudyMaterialSetResponse;
import com.education.web.materials.model.StudyMaterialLessonEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/study-materials")
public class StudentStudyMaterialController {

    private final StudentStudyMaterialService service;

    public StudentStudyMaterialController(StudentStudyMaterialService service) {
        this.service = service;
    }

    @GetMapping("/sets")
    public List<StudyMaterialSetResponse> listSets(@RequestParam("userId") String userId) {
        return service.listSets(userId);
    }

    @GetMapping("/sets/{setId}/lessons")
    public List<StudyMaterialLessonResponse> listLessons(
            @RequestParam("userId") String userId,
            @PathVariable("setId") String setId
    ) {
        return service.listLessons(userId, setId);
    }

    @GetMapping("/lessons/{lessonId}/pdf")
    public ResponseEntity<Resource> lessonPdf(
            @RequestParam("userId") String userId,
            @PathVariable("lessonId") String lessonId,
            @RequestParam(value = "inline", defaultValue = "true") boolean inline
    ) {
        StudyMaterialLessonEntity le = service.requireLessonForStudentDownload(userId, lessonId);
        return StudyMaterialPdfHttpResponses.pdf(le.getPdfData(), le.getFileName(), le.getContentType(), inline);
    }
}
