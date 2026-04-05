package com.education.web.schooladmin;

import com.education.web.schooladmin.dto.AddStudentToGroupRequest;
import com.education.web.schooladmin.dto.CreateSchoolGroupRequest;
import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import com.education.web.schooladmin.service.SchoolAdminGroupsService;
import com.education.web.schooladmin.service.SchoolAdminStudentGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school-admin")
public class SchoolAdminGroupsController {

    private final SchoolAdminGroupsService groupsService;
    private final SchoolAdminStudentGroupService studentGroupService;

    public SchoolAdminGroupsController(
            SchoolAdminGroupsService groupsService,
            SchoolAdminStudentGroupService studentGroupService
    ) {
        this.groupsService = groupsService;
        this.studentGroupService = studentGroupService;
    }

    @PostMapping("/groups")
    public SchoolGroupCardResponse createGroup(
            @RequestParam("schoolId") String schoolId,
            @RequestBody CreateSchoolGroupRequest request
    ) {
        return groupsService.createGroup(schoolId, request);
    }

    /**
     * Зарахування студента в групу. Параметри в query (як у інших /school-admin), без UUID у шляху —
     * так менше шансів на 404 через проксі / стару збірку.
     */
    @PostMapping("/groups/students")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addStudentToGroup(
            @RequestParam("schoolId") String schoolId,
            @RequestParam("groupId") String groupId,
            @Valid @RequestBody AddStudentToGroupRequest body
    ) {
        studentGroupService.enrollStudent(schoolId, groupId, body.studentId());
    }
}

