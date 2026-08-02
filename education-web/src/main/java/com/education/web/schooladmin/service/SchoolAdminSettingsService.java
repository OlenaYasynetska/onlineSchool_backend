package com.education.web.schooladmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.grading.GradingMethod;
import com.education.web.grading.GradingScale;
import com.education.web.schooladmin.dto.SchoolSettingsResponse;
import com.education.web.schooladmin.dto.UpdateSchoolSettingsRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SchoolAdminSettingsService {

    private final OrganizationJpaRepository organizations;

    public SchoolAdminSettingsService(OrganizationJpaRepository organizations) {
        this.organizations = organizations;
    }

    @Transactional(readOnly = true)
    public SchoolSettingsResponse getSettings(String schoolId) {
        OrganizationEntity org = requireSchool(schoolId);
        return toResponse(org);
    }

    @Transactional
    public SchoolSettingsResponse updateSettings(String schoolId, UpdateSchoolSettingsRequest req) {
        OrganizationEntity org = requireSchool(schoolId);
        if (req == null || req.gradingMethod() == null || req.gradingMethod().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gradingMethod is required");
        }
        if (req.gradingScale() == null || req.gradingScale().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gradingScale is required");
        }
        GradingMethod method = GradingMethod.fromWire(req.gradingMethod());
        GradingScale scale = GradingScale.fromWire(req.gradingScale());
        org.setGradingMethod(method.wireValue());
        org.setGradingScale(scale.wireValue());
        organizations.save(org);
        return toResponse(org);
    }

    private OrganizationEntity requireSchool(String schoolId) {
        if (schoolId == null || schoolId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "schoolId is required");
        }
        return organizations.findById(schoolId.trim()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found")
        );
    }

    private static SchoolSettingsResponse toResponse(OrganizationEntity org) {
        return new SchoolSettingsResponse(
                org.getId(),
                GradingMethod.fromWire(org.getGradingMethod()).wireValue(),
                GradingScale.fromWire(org.getGradingScale()).wireValue()
        );
    }
}
