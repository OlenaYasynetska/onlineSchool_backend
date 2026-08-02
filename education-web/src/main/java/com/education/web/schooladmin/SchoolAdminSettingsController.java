package com.education.web.schooladmin;

import com.education.web.schooladmin.dto.SchoolSettingsResponse;
import com.education.web.schooladmin.dto.UpdateSchoolSettingsRequest;
import com.education.web.schooladmin.service.SchoolAdminSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school-admin")
public class SchoolAdminSettingsController {

    private final SchoolAdminSettingsService settingsService;

    public SchoolAdminSettingsController(SchoolAdminSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/settings")
    public SchoolSettingsResponse settings(@RequestParam("schoolId") String schoolId) {
        return settingsService.getSettings(schoolId);
    }

    @PutMapping("/settings")
    public SchoolSettingsResponse updateSettingsPut(
            @RequestParam("schoolId") String schoolId,
            @RequestBody UpdateSchoolSettingsRequest body
    ) {
        return settingsService.updateSettings(schoolId, body);
    }

    @PostMapping("/settings")
    public SchoolSettingsResponse updateSettingsPost(
            @RequestParam("schoolId") String schoolId,
            @RequestBody UpdateSchoolSettingsRequest body
    ) {
        return settingsService.updateSettings(schoolId, body);
    }

    @PatchMapping("/settings")
    public SchoolSettingsResponse updateSettings(
            @RequestParam("schoolId") String schoolId,
            @RequestBody UpdateSchoolSettingsRequest body
    ) {
        return settingsService.updateSettings(schoolId, body);
    }
}
