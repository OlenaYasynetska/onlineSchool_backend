package com.education.web.schooladmin.service;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.SchoolSubjectEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.SchoolSubjectJpaRepository;
import com.education.web.schooladmin.dto.CreateSchoolSubjectRequest;
import com.education.web.schooladmin.dto.SchoolSubjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SchoolAdminSubjectsService {

    private final OrganizationJpaRepository organizations;
    private final SchoolSubjectJpaRepository subjects;

    public SchoolAdminSubjectsService(
            OrganizationJpaRepository organizations,
            SchoolSubjectJpaRepository subjects
    ) {
        this.organizations = organizations;
        this.subjects = subjects;
    }

    @Transactional(readOnly = true)
    public List<SchoolSubjectResponse> listSubjects(String schoolId) {
        ensureSchoolExists(schoolId);
        return subjects.findByOrganization_IdOrderByTitleAsc(schoolId).stream()
                .map(s -> new SchoolSubjectResponse(s.getId(), s.getTitle()))
                .toList();
    }

    @Transactional
    public SchoolSubjectResponse createSubject(String schoolId, CreateSchoolSubjectRequest req) {
        OrganizationEntity org = ensureSchoolExists(schoolId);
        String title = req.title() != null ? req.title().trim() : "";
        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing title");
        }
        return subjects.findByOrganization_IdAndTitleIgnoreCase(schoolId, title)
                .map(s -> new SchoolSubjectResponse(s.getId(), s.getTitle()))
                .orElseGet(() -> {
                    SchoolSubjectEntity e = new SchoolSubjectEntity();
                    e.setOrganization(org);
                    e.setTitle(title);
                    SchoolSubjectEntity saved = subjects.save(e);
                    return new SchoolSubjectResponse(saved.getId(), saved.getTitle());
                });
    }

    private OrganizationEntity ensureSchoolExists(String schoolId) {
        return organizations.findById(schoolId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found")
        );
    }
}
