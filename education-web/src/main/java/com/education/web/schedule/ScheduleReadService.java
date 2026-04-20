package com.education.web.schedule;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.SchoolGroupScheduleEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.SchoolGroupScheduleJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.schedule.dto.ScheduleSlotResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ScheduleReadService {

    private final TeacherJpaRepository teachers;
    private final SpringDataStudentJpaRepository students;
    private final SchoolGroupStudentJpaRepository groupStudents;
    private final SchoolGroupScheduleJpaRepository schedule;

    public ScheduleReadService(
            TeacherJpaRepository teachers,
            SpringDataStudentJpaRepository students,
            SchoolGroupStudentJpaRepository groupStudents,
            SchoolGroupScheduleJpaRepository schedule
    ) {
        this.teachers = teachers;
        this.students = students;
        this.groupStudents = groupStudents;
        this.schedule = schedule;
    }

    @Transactional(readOnly = true)
    public List<ScheduleSlotResponse> listForTeacherUser(String userId) {
        TeacherEntity t = teachers.findByUser_Id(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found for this user")
        );
        String schoolId = t.getSchool().getId();
        List<SchoolGroupScheduleEntity> rows = schedule.findForTeacherInSchool(schoolId, t.getId());
        return rows.stream().map(ScheduleSlotMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleSlotResponse> listForStudentUser(String userId) {
        StudentJpaEntity st = students.findByUserId(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found for this user")
        );
        List<String> groupIds = groupStudents.findByStudentIdFetchGroup(st.getId()).stream()
                .map(l -> l.getGroup().getId())
                .distinct()
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return schedule.findByGroup_IdInWithDetails(groupIds).stream()
                .map(ScheduleSlotMapper::toResponse)
                .toList();
    }
}
