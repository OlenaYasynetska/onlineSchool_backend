package com.education.web.schedule;

import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupScheduleEntity;
import com.education.web.auth.model.SchoolSubjectEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupScheduleJpaRepository;
import com.education.web.auth.repository.SchoolSubjectJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.schedule.dto.ScheduleSlotResponse;
import com.education.web.schedule.dto.UpsertScheduleSlotRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class SchoolAdminScheduleService {

    private final OrganizationJpaRepository organizations;
    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolGroupScheduleJpaRepository schedule;
    private final TeacherJpaRepository teachers;
    private final SchoolSubjectJpaRepository subjects;

    public SchoolAdminScheduleService(
            OrganizationJpaRepository organizations,
            SchoolGroupJpaRepository schoolGroups,
            SchoolGroupScheduleJpaRepository schedule,
            TeacherJpaRepository teachers,
            SchoolSubjectJpaRepository subjects
    ) {
        this.organizations = organizations;
        this.schoolGroups = schoolGroups;
        this.schedule = schedule;
        this.teachers = teachers;
        this.subjects = subjects;
    }

    @Transactional(readOnly = true)
    public List<ScheduleSlotResponse> list(String schoolId) {
        requireSchool(schoolId);
        return schedule.findAllForSchoolWithDetails(schoolId).stream()
                .map(ScheduleSlotMapper::toResponse)
                .toList();
    }

    @Transactional
    public ScheduleSlotResponse create(String schoolId, UpsertScheduleSlotRequest req) {
        requireSchool(schoolId);
        SchoolGroupEntity group = schoolGroups.findById(req.getGroupId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        if (!group.getOrganization().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group does not belong to this school");
        }
        TeacherEntity teacher = teachers.findByIdAndSchool_Id(req.getTeacherId().trim(), schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher not found in this school"));
        SchoolSubjectEntity subjectEntity = resolveSubject(schoolId, req.getSubjectId());

        LocalTime start = parseTime(req.getStartTime(), "startTime");
        LocalTime end = parseTime(req.getEndTime(), "endTime");
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        SchoolGroupScheduleEntity row = new SchoolGroupScheduleEntity();
        row.setGroup(group);
        row.setTeacher(teacher);
        row.setSubject(subjectEntity);
        row.setDayOfWeek(req.getDayOfWeek());
        row.setStartTime(start);
        row.setEndTime(end);
        row.setValidFrom(parseOptDate(req.getValidFrom(), "validFrom"));
        row.setValidUntil(parseOptDate(req.getValidUntil(), "validUntil"));
        row.setNotes(trimToNull(req.getNotes()));
        row.setRoom(trimToNull(req.getRoom()));

        schedule.save(row);
        return ScheduleSlotMapper.toResponse(row);
    }

    @Transactional
    public ScheduleSlotResponse update(String schoolId, String slotId, UpsertScheduleSlotRequest req) {
        requireSchool(schoolId);
        SchoolGroupScheduleEntity row = schedule.findOneByIdAndSchoolId(slotId, schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule slot not found"));

        SchoolGroupEntity group = schoolGroups.findById(req.getGroupId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        if (!group.getOrganization().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group does not belong to this school");
        }
        TeacherEntity teacher = teachers.findByIdAndSchool_Id(req.getTeacherId().trim(), schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher not found in this school"));
        SchoolSubjectEntity subjectEntity = resolveSubject(schoolId, req.getSubjectId());

        LocalTime start = parseTime(req.getStartTime(), "startTime");
        LocalTime end = parseTime(req.getEndTime(), "endTime");
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        row.setGroup(group);
        row.setTeacher(teacher);
        row.setSubject(subjectEntity);
        row.setDayOfWeek(req.getDayOfWeek());
        row.setStartTime(start);
        row.setEndTime(end);
        row.setValidFrom(parseOptDate(req.getValidFrom(), "validFrom"));
        row.setValidUntil(parseOptDate(req.getValidUntil(), "validUntil"));
        row.setNotes(trimToNull(req.getNotes()));
        row.setRoom(trimToNull(req.getRoom()));

        return ScheduleSlotMapper.toResponse(row);
    }

    @Transactional
    public void delete(String schoolId, String slotId) {
        requireSchool(schoolId);
        SchoolGroupScheduleEntity row = schedule.findOneByIdAndSchoolId(slotId, schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule slot not found"));
        schedule.delete(row);
    }

    private void requireSchool(String schoolId) {
        organizations.findById(schoolId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found")
        );
    }

    private SchoolSubjectEntity resolveSubject(String schoolId, String subjectIdRaw) {
        String sid = trimToNull(subjectIdRaw);
        if (sid == null) {
            return null;
        }
        SchoolSubjectEntity s = subjects.findById(sid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject not found"));
        if (!s.getOrganization().getId().equals(schoolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject does not belong to this school");
        }
        return s;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalDate parseOptDate(String raw, String field) {
        String t = trimToNull(raw);
        if (t == null) {
            return null;
        }
        try {
            return LocalDate.parse(t);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + field + ", use yyyy-MM-dd");
        }
    }

    private static LocalTime parseTime(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        String t = raw.trim();
        try {
            if (t.length() == 5 && t.charAt(2) == ':') {
                return LocalTime.parse(t + ":00");
            }
            return LocalTime.parse(t);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + field + ", use HH:mm");
        }
    }
}
