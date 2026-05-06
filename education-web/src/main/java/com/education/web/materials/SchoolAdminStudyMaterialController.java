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
@RequestMapping("/api/school-admin/study-materials")
public class SchoolAdminStudyMaterialController {

    private final SchoolAdminStudyMaterialService service;

    public SchoolAdminStudyMaterialController(SchoolAdminStudyMaterialService service) {
        this.service = service;
    }

    @GetMapping("/sets")
    public List<StudyMaterialSetResponse> listSets(@RequestParam("schoolId") String schoolId) {
        return service.listSets(schoolId);
    }

    @GetMapping("/sets/{setId}/lessons")
    public List<StudyMaterialLessonResponse> listLessons(
            @RequestParam("schoolId") String schoolId,
            @PathVariable("setId") String setId
    ) {
        return service.listLessons(schoolId, setId);
    }

    @GetMapping("/lessons/{lessonId}/pdf")
    public ResponseEntity<Resource> lessonPdf(
            @RequestParam("schoolId") String schoolId,
            @PathVariable("lessonId") String lessonId,
            @RequestParam(value = "inline", defaultValue = "true") boolean inline
    ) {
        StudyMaterialLessonEntity le = service.requireLessonForSchoolDownload(schoolId, lessonId);
        return StudyMaterialPdfHttpResponses.pdf(le.getPdfData(), le.getFileName(), le.getContentType(), inline);
    }
}
