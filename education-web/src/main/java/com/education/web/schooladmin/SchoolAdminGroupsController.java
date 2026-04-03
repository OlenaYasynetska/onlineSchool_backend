package com.education.web.schooladmin;

import com.education.web.schooladmin.dto.CreateSchoolGroupRequest;
import com.education.web.schooladmin.dto.SchoolGroupCardResponse;
import com.education.web.schooladmin.service.SchoolAdminGroupsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school-admin")
public class SchoolAdminGroupsController {

    private final SchoolAdminGroupsService groupsService;

    public SchoolAdminGroupsController(SchoolAdminGroupsService groupsService) {
        this.groupsService = groupsService;
    }

    @PostMapping("/groups")
    public SchoolGroupCardResponse createGroup(
            @RequestParam("schoolId") String schoolId,
            @RequestBody CreateSchoolGroupRequest request
    ) {
        return groupsService.createGroup(schoolId, request);
    }
}

