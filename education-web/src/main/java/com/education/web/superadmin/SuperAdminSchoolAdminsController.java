package com.education.web.superadmin;

import com.education.web.superadmin.dto.SchoolAdminContactResponse;
import com.education.web.superadmin.dto.SchoolAdminUpdateRequest;
import com.education.web.superadmin.service.SuperAdminSchoolAdminsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminSchoolAdminsController {
    private final SuperAdminSchoolAdminsService schoolAdminsService;

    public SuperAdminSchoolAdminsController(SuperAdminSchoolAdminsService schoolAdminsService) {
        this.schoolAdminsService = schoolAdminsService;
    }

    @GetMapping("/school-admins")
    public List<SchoolAdminContactResponse> listSchoolAdmins() {
        return schoolAdminsService.listSchoolAdmins();
    }

    @PutMapping("/school-admins/{userId}")
    public SchoolAdminContactResponse updateSchoolAdmin(
            @PathVariable("userId") String userId,
            @Valid @RequestBody SchoolAdminUpdateRequest body
    ) {
        return schoolAdminsService.updateSchoolAdmin(userId, body);
    }
}
