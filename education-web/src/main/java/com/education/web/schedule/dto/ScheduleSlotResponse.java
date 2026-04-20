package com.education.web.schedule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Слот розкладу для UI (тижнева сітка + модалки).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScheduleSlotResponse(
        String id,
        String groupId,
        String groupName,
        String teacherId,
        String teacherDisplayName,
        String subjectId,
        String subjectTitle,
        int dayOfWeek,
        String startTime,
        String endTime,
        String validFrom,
        String validUntil,
        String notes,
        String room
) {}
