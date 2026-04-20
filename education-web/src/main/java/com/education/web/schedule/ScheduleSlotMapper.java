package com.education.web.schedule;

import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupScheduleEntity;
import com.education.web.auth.model.SchoolSubjectEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.schedule.dto.ScheduleSlotResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

final class ScheduleSlotMapper {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private ScheduleSlotMapper() {}

    static ScheduleSlotResponse toResponse(SchoolGroupScheduleEntity s) {
        SchoolGroupEntity g = s.getGroup();
        TeacherEntity slotTeacher = s.getTeacher();
        TeacherEntity groupTeacher = g.getTeacher();
        TeacherEntity effective = slotTeacher != null ? slotTeacher : groupTeacher;

        String teacherId = effective != null ? effective.getId() : "";
        String teacherName = displayName(effective);

        SchoolSubjectEntity slotSubj = s.getSubject();
        SchoolSubjectEntity groupSubj = g.getSubject();
        SchoolSubjectEntity subj = slotSubj != null ? slotSubj : groupSubj;
        String subjectId = subj != null ? subj.getId() : null;
        String subjectTitle = subj != null ? subj.getTitle() : null;

        LocalDate vf = s.getValidFrom();
        LocalDate vu = s.getValidUntil();

        return new ScheduleSlotResponse(
                s.getId(),
                g.getId(),
                g.getName(),
                teacherId,
                teacherName,
                subjectId,
                subjectTitle,
                s.getDayOfWeek(),
                formatTime(s.getStartTime()),
                formatTime(s.getEndTime()),
                vf != null ? DATE_FMT.format(vf) : null,
                vu != null ? DATE_FMT.format(vu) : null,
                s.getNotes(),
                s.getRoom()
        );
    }

    private static String displayName(TeacherEntity t) {
        if (t == null) {
            return "";
        }
        UserEntity u = t.getUser();
        if (u == null) {
            return "";
        }
        String name = (u.getFirstName() + " " + u.getLastName()).trim();
        return name.isEmpty() ? u.getEmail() : name;
    }

    private static String formatTime(LocalTime t) {
        return t == null ? "" : TIME_FMT.format(t);
    }
}
