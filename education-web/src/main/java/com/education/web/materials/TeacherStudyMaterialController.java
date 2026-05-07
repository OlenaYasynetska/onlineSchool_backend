package com.education.web.materials;

import com.education.web.materials.dto.CreateStudyMaterialSetRequest;
import com.education.web.materials.dto.StudyMaterialLessonResponse;
import com.education.web.materials.dto.StudyMaterialSetResponse;
import com.education.web.materials.dto.TeacherSubjectOptionResponse;
import com.education.web.materials.model.StudyMaterialLessonEntity;
import com.education.web.materials.dto.UpdateStudyMaterialLessonRequest;
import com.education.web.materials.dto.UpdateStudyMaterialSetRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/study-materials")
public class TeacherStudyMaterialController {

    private final TeacherStudyMaterialService service;

    public TeacherStudyMaterialController(TeacherStudyMaterialService service) {
        this.service = service;
    }

    @GetMapping("/subjects")
    public List<TeacherSubjectOptionResponse> subjects(@RequestParam("userId") String userId) {
        return service.listSubjects(userId);
    }

    @GetMapping("/sets")
    public List<StudyMaterialSetResponse> listSets(@RequestParam("userId") String userId) {
        return service.listSets(userId);
    }

    @PostMapping("/sets")
    public StudyMaterialSetResponse createSet(
            @RequestParam("userId") String userId,
            @Valid @RequestBody CreateStudyMaterialSetRequest body
    ) {
        return service.createSet(userId, body);
    }

    /**
     * PATCH та PUT — одна й та ж логіка (PUT зручніший для частини проксі / CDN).
     */
    @RequestMapping(value = "/sets/{setId}", method = { RequestMethod.PATCH, RequestMethod.PUT })
    public StudyMaterialSetResponse updateSet(
            @RequestParam("userId") String userId,
            @PathVariable("setId") String setId,
            @Valid @RequestBody UpdateStudyMaterialSetRequest body
    ) {
        return service.updateSet(userId, setId, body);
    }

    @RequestMapping(value = "/lessons/{lessonId}", method = { RequestMethod.PATCH, RequestMethod.PUT })
    public StudyMaterialLessonResponse updateLesson(
            @RequestParam("userId") String userId,
            @PathVariable("lessonId") String lessonId,
            @Valid @RequestBody UpdateStudyMaterialLessonRequest body
    ) {
        return service.updateLesson(userId, lessonId, body);
    }

    @DeleteMapping("/sets/{setId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSet(@RequestParam("userId") String userId, @PathVariable("setId") String setId) {
        service.deleteSet(userId, setId);
    }

    @DeleteMapping("/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@RequestParam("userId") String userId, @PathVariable("lessonId") String lessonId) {
        service.deleteLesson(userId, lessonId);
    }

    @GetMapping("/sets/{setId}/lessons")
    public List<StudyMaterialLessonResponse> listLessons(
            @RequestParam("userId") String userId,
            @PathVariable("setId") String setId
    ) {
        return service.listLessons(userId, setId);
    }

    @PostMapping(value = "/sets/{setId}/lessons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudyMaterialLessonResponse addLesson(
            @RequestParam("userId") String userId,
            @PathVariable("setId") String setId,
            @RequestParam("title") String title,
            @RequestPart("file") MultipartFile file
    ) {
        return service.addPdfLesson(userId, setId, title, file);
    }

    @GetMapping("/lessons/{lessonId}/pdf")
    public ResponseEntity<Resource> lessonPdf(
            @RequestParam("userId") String userId,
            @PathVariable("lessonId") String lessonId,
            @RequestParam(value = "inline", defaultValue = "true") boolean inline
    ) {
        StudyMaterialLessonEntity le = service.requireLessonForTeacherDownload(userId, lessonId);
        return StudyMaterialPdfHttpResponses.pdf(le.getPdfData(), le.getFileName(), le.getContentType(), inline);
    }
}
