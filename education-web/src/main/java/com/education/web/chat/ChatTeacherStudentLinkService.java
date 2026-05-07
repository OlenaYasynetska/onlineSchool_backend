package com.education.web.chat;

import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Учень і вчитель можуть мати приватний чат лише якщо учень зарахований у групу,
 * де цей вчитель призначений ({@code school_groups.teacher_id}).
 */
@Service
@ConditionalOnProperty(name = "education.chat.mongodb-enabled", havingValue = "true")
public class ChatTeacherStudentLinkService {

    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolGroupStudentJpaRepository groupStudents;

    public ChatTeacherStudentLinkService(
            SchoolGroupJpaRepository schoolGroups,
            SchoolGroupStudentJpaRepository groupStudents) {
        this.schoolGroups = schoolGroups;
        this.groupStudents = groupStudents;
    }

    public boolean sharesGroup(String teacherRecordId, String studentRecordId) {
        List<SchoolGroupEntity> groups = schoolGroups.findByTeacher_IdOrderByNameAsc(teacherRecordId);
        for (SchoolGroupEntity g : groups) {
            TeacherEntity t = g.getTeacher();
            if (t == null || !teacherRecordId.equals(t.getId())) {
                continue;
            }
            if (groupStudents.existsByStudentIdAndGroup_Id(studentRecordId, g.getId())) {
                return true;
            }
        }
        return false;
    }
}
