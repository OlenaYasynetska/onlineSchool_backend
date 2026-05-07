package com.education.web.materials;

import com.education.web.auth.model.TeacherSubjectEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.materials.dto.StudyMaterialLessonResponse;
import com.education.web.materials.dto.StudyMaterialSetResponse;
import com.education.web.materials.model.StudyMaterialLessonEntity;
import com.education.web.materials.model.StudyMaterialSetEntity;
import com.education.web.materials.repository.StudyMaterialLessonJpaRepository;
import com.education.web.materials.repository.StudyMaterialSetJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SchoolAdminStudyMaterialService {

    private final OrganizationJpaRepository organizations;
    private final StudyMaterialSetJpaRepository sets;
    private final StudyMaterialLessonJpaRepository lessons;

    public SchoolAdminStudyMaterialService(
            OrganizationJpaRepository organizations,
            StudyMaterialSetJpaRepository sets,
            StudyMaterialLessonJpaRepository lessons
    ) {
        this.organizations = organizations;
        this.sets = sets;
        this.lessons = lessons;
    }

    private void requireSchool(String schoolId) {
        if (schoolId == null || schoolId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing schoolId");
        }
        if (organizations.findById(schoolId.trim()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found");
        }
    }

    private void assertSetInSchool(String schoolId, StudyMaterialSetEntity set) {
        if (!set.getSchool().getId().equals(schoolId.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Material set not in this school");
        }
    }

    @Transactional(readOnly = true)
    public List<StudyMaterialSetResponse> listSets(String schoolId) {
        requireSchool(schoolId);
        return sets.findBySchool_IdOrderByUpdatedAtDesc(schoolId.trim()).stream()
                .map(this::toSetResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudyMaterialLessonResponse> listLessons(String schoolId, String setId) {
        requireSchool(schoolId);
        StudyMaterialSetEntity set = sets.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material set not found"));
        assertSetInSchool(schoolId, set);
        return lessons.findByMaterialSet_IdOrderBySortOrderAsc(setId).stream()
                .map(l -> new StudyMaterialLessonResponse(
                        l.getId(),
                        l.getTitle(),
                        l.getSortOrder(),
                        l.getFileName(),
                        l.getIssuuEmbedUrl()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyMaterialLessonEntity requireLessonForSchoolDownload(String schoolId, String lessonId) {
        requireSchool(schoolId);
        StudyMaterialLessonEntity le = lessons.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        assertSetInSchool(schoolId, le.getMaterialSet());
        return le;
    }

    private StudyMaterialSetResponse toSetResponse(StudyMaterialSetEntity e) {
        long c = lessons.countByMaterialSet_Id(e.getId());
        TeacherSubjectEntity ts = e.getTeacherSubject();
        String sid = ts != null ? ts.getId() : null;
        String stitle = ts != null ? ts.getTitle() : null;
        return new StudyMaterialSetResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                sid,
                stitle,
                (int) Math.min(c, Integer.MAX_VALUE),
                e.getUpdatedAt()
        );
    }
}
