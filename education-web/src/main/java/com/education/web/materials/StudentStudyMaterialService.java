package com.education.web.materials;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.materials.dto.StudyMaterialLessonResponse;
import com.education.web.materials.dto.StudyMaterialSetResponse;
import com.education.web.materials.model.StudyMaterialLessonEntity;
import com.education.web.materials.model.StudyMaterialSetEntity;
import com.education.web.auth.model.TeacherSubjectEntity;
import com.education.web.materials.repository.StudyMaterialLessonJpaRepository;
import com.education.web.materials.repository.StudyMaterialSetJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentStudyMaterialService {

    private final SpringDataStudentJpaRepository students;
    private final StudyMaterialSetJpaRepository sets;
    private final StudyMaterialLessonJpaRepository lessons;

    public StudentStudyMaterialService(
            SpringDataStudentJpaRepository students,
            StudyMaterialSetJpaRepository sets,
            StudyMaterialLessonJpaRepository lessons
    ) {
        this.students = students;
        this.sets = sets;
        this.lessons = lessons;
    }

    private StudentJpaEntity requireStudent(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        return students.findByUserId(userId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
    }

    private void assertSetInStudentSchool(StudentJpaEntity st, StudyMaterialSetEntity set) {
        if (!st.getSchoolId().equals(set.getSchool().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Material set not in your school");
        }
    }

    @Transactional(readOnly = true)
    public List<StudyMaterialSetResponse> listSets(String userId) {
        StudentJpaEntity st = requireStudent(userId);
        return sets.findBySchool_IdOrderByUpdatedAtDesc(st.getSchoolId()).stream()
                .map(this::toSetResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudyMaterialLessonResponse> listLessons(String userId, String setId) {
        StudentJpaEntity st = requireStudent(userId);
        StudyMaterialSetEntity set = sets.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material set not found"));
        assertSetInStudentSchool(st, set);
        return lessons.findByMaterialSet_IdOrderBySortOrderAsc(setId).stream()
                .map(l -> new StudyMaterialLessonResponse(l.getId(), l.getTitle(), l.getSortOrder(), l.getFileName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyMaterialLessonEntity requireLessonForStudentDownload(String userId, String lessonId) {
        StudentJpaEntity st = requireStudent(userId);
        StudyMaterialLessonEntity le = lessons.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        assertSetInStudentSchool(st, le.getMaterialSet());
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
