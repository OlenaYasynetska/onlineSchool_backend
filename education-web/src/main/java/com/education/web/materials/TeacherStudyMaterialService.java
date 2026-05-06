package com.education.web.materials;

import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.TeacherSubjectEntity;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.TeacherSubjectJpaRepository;
import com.education.web.materials.dto.CreateStudyMaterialSetRequest;
import com.education.web.materials.dto.StudyMaterialLessonResponse;
import com.education.web.materials.dto.StudyMaterialSetResponse;
import com.education.web.materials.dto.TeacherSubjectOptionResponse;
import com.education.web.materials.dto.UpdateStudyMaterialLessonRequest;
import com.education.web.materials.dto.UpdateStudyMaterialSetRequest;
import com.education.web.materials.model.StudyMaterialLessonEntity;
import com.education.web.materials.model.StudyMaterialSetEntity;
import com.education.web.materials.repository.StudyMaterialLessonJpaRepository;
import com.education.web.materials.repository.StudyMaterialSetJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class TeacherStudyMaterialService {

    private static final long MAX_PDF_BYTES = 25L * 1024 * 1024;

    private final TeacherJpaRepository teachers;
    private final TeacherSubjectJpaRepository teacherSubjects;
    private final StudyMaterialSetJpaRepository sets;
    private final StudyMaterialLessonJpaRepository lessons;

    public TeacherStudyMaterialService(
            TeacherJpaRepository teachers,
            TeacherSubjectJpaRepository teacherSubjects,
            StudyMaterialSetJpaRepository sets,
            StudyMaterialLessonJpaRepository lessons
    ) {
        this.teachers = teachers;
        this.teacherSubjects = teacherSubjects;
        this.sets = sets;
        this.lessons = lessons;
    }

    private TeacherEntity requireTeacher(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        return teachers.findByUser_Id(userId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher profile not found"));
    }

    @Transactional(readOnly = true)
    public List<TeacherSubjectOptionResponse> listSubjects(String userId) {
        TeacherEntity t = requireTeacher(userId);
        return teacherSubjects.findByTeacher_IdOrderBySortOrderAsc(t.getId()).stream()
                .map(s -> new TeacherSubjectOptionResponse(s.getId(), s.getTitle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudyMaterialSetResponse> listSets(String userId) {
        TeacherEntity t = requireTeacher(userId);
        return sets.findByTeacher_IdOrderByUpdatedAtDesc(t.getId()).stream()
                .map(this::toSetResponse)
                .toList();
    }

    @Transactional
    public StudyMaterialSetResponse createSet(String userId, CreateStudyMaterialSetRequest body) {
        TeacherEntity t = requireTeacher(userId);
        TeacherSubjectEntity ts = teacherSubjects
                .findByIdAndTeacher_Id(body.teacherSubjectId().trim(), t.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown subject for this teacher"));

        StudyMaterialSetEntity row = new StudyMaterialSetEntity();
        row.setSchool(t.getSchool());
        row.setTeacher(t);
        row.setTeacherSubject(ts);
        row.setTitle(body.title().trim());
        String desc = body.description();
        row.setDescription(desc != null && !desc.isBlank() ? desc.trim() : null);
        row = sets.save(row);
        return toSetResponse(row);
    }

    @Transactional(readOnly = true)
    public List<StudyMaterialLessonResponse> listLessons(String userId, String setId) {
        TeacherEntity t = requireTeacher(userId);
        StudyMaterialSetEntity set = sets.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material set not found"));
        if (!set.getTeacher().getId().equals(t.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your material set");
        }
        return lessons.findByMaterialSet_IdOrderBySortOrderAsc(setId).stream()
                .map(l -> new StudyMaterialLessonResponse(l.getId(), l.getTitle(), l.getSortOrder(), l.getFileName()))
                .toList();
    }

    @Transactional
    public StudyMaterialLessonResponse addPdfLesson(String userId, String setId, String title, MultipartFile file) {
        TeacherEntity t = requireTeacher(userId);
        StudyMaterialSetEntity set = sets.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material set not found"));
        if (!set.getTeacher().getId().equals(t.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your material set");
        }
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing lesson title");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing PDF file");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read file");
        }
        if (bytes.length > MAX_PDF_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF must be at most 25 MB");
        }
        String orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "lesson.pdf";
        if (!isPdfAllowed(file.getContentType(), orig)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        List<StudyMaterialLessonEntity> existing = lessons.findByMaterialSet_IdOrderBySortOrderAsc(setId);
        int nextOrder = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;

        StudyMaterialLessonEntity le = new StudyMaterialLessonEntity();
        le.setMaterialSet(set);
        le.setTitle(title.trim());
        le.setSortOrder(nextOrder);
        le.setPdfData(bytes);
        le.setFileName(orig);
        String ct = file.getContentType();
        le.setContentType(ct != null && !ct.isBlank() ? ct : "application/pdf");
        lessons.save(le);
        set.setUpdatedAt(Instant.now());
        sets.save(set);

        return new StudyMaterialLessonResponse(le.getId(), le.getTitle(), le.getSortOrder(), le.getFileName());
    }

    @Transactional(readOnly = true)
    public StudyMaterialLessonEntity requireLessonForTeacherDownload(String userId, String lessonId) {
        TeacherEntity t = requireTeacher(userId);
        StudyMaterialLessonEntity le = lessons.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (!le.getMaterialSet().getTeacher().getId().equals(t.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your lesson");
        }
        return le;
    }

    @Transactional
    public StudyMaterialSetResponse updateSet(String userId, String setId, UpdateStudyMaterialSetRequest body) {
        TeacherEntity t = requireTeacher(userId);
        StudyMaterialSetEntity set = sets.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material set not found"));
        if (!set.getTeacher().getId().equals(t.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your material set");
        }
        set.setTitle(body.title().trim());
        String desc = body.description();
        set.setDescription(desc == null || desc.isBlank() ? null : desc.trim());
        set.setUpdatedAt(Instant.now());
        return toSetResponse(sets.save(set));
    }

    @Transactional
    public StudyMaterialLessonResponse updateLesson(
            String userId,
            String lessonId,
            UpdateStudyMaterialLessonRequest body
    ) {
        TeacherEntity t = requireTeacher(userId);
        StudyMaterialLessonEntity le = lessons.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (!le.getMaterialSet().getTeacher().getId().equals(t.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your lesson");
        }
        le.setTitle(body.title().trim());
        le.setUpdatedAt(Instant.now());
        StudyMaterialSetEntity matSet = le.getMaterialSet();
        matSet.setUpdatedAt(Instant.now());
        sets.save(matSet);
        lessons.save(le);
        return new StudyMaterialLessonResponse(le.getId(), le.getTitle(), le.getSortOrder(), le.getFileName());
    }

    @Transactional
    public void deleteSet(String userId, String setId) {
        TeacherEntity t = requireTeacher(userId);
        StudyMaterialSetEntity set = sets.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material set not found"));
        if (!set.getTeacher().getId().equals(t.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your material set");
        }
        sets.delete(set);
    }

    @Transactional
    public void deleteLesson(String userId, String lessonId) {
        TeacherEntity t = requireTeacher(userId);
        StudyMaterialLessonEntity le = lessons.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (!le.getMaterialSet().getTeacher().getId().equals(t.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your lesson");
        }
        StudyMaterialSetEntity set = le.getMaterialSet();
        lessons.delete(le);
        set.setUpdatedAt(Instant.now());
        sets.save(set);
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

    private static boolean isPdfAllowed(String contentType, String fileName) {
        if (contentType != null && contentType.toLowerCase().contains("pdf")) {
            return true;
        }
        return fileName.toLowerCase().endsWith(".pdf");
    }
}
