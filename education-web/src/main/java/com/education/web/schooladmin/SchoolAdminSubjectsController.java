package com.education.web.schooladmin;

import com.education.web.schooladmin.dto.CreateSchoolSubjectRequest;
import com.education.web.schooladmin.dto.SchoolSubjectResponse;
import com.education.web.schooladmin.service.SchoolAdminSubjectsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/school-admin")
public class SchoolAdminSubjectsController {

    private final SchoolAdminSubjectsService subjectsService;

    public SchoolAdminSubjectsController(SchoolAdminSubjectsService subjectsService) {
        this.subjectsService = subjectsService;
    }

    @GetMapping("/subjects")
    public List<SchoolSubjectResponse> listSubjects(@RequestParam("schoolId") String schoolId) {
        return subjectsService.listSubjects(schoolId);
    }

    @PostMapping("/subjects")
    public SchoolSubjectResponse createSubject(
            @RequestParam("schoolId") String schoolId,
            @RequestBody CreateSchoolSubjectRequest request
    ) {
        return subjectsService.createSubject(schoolId, request);
    }
}
