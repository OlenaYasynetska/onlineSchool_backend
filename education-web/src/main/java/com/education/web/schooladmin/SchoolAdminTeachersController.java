package com.education.web.schooladmin;

import com.education.web.schooladmin.dto.CreateSchoolTeacherRequest;
import com.education.web.schooladmin.dto.SchoolTeacherOptionResponse;
import com.education.web.schooladmin.service.SchoolAdminTeachersService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/school-admin")
public class SchoolAdminTeachersController {

    private final SchoolAdminTeachersService teachersService;

    public SchoolAdminTeachersController(SchoolAdminTeachersService teachersService) {
        this.teachersService = teachersService;
    }

    @GetMapping("/teachers")
    public List<SchoolTeacherOptionResponse> listTeachers(@RequestParam("schoolId") String schoolId) {
        return teachersService.listTeachers(schoolId);
    }

    @PostMapping("/teachers")
    public SchoolTeacherOptionResponse createTeacher(
            @RequestParam("schoolId") String schoolId,
            @RequestBody CreateSchoolTeacherRequest request
    ) {
        return teachersService.createTeacher(schoolId, request);
    }
}
