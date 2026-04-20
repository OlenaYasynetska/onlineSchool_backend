package com.education.web.schooladmin.dto;

import java.util.List;

/**
 * Викладач школи: список у формі групи + розширений рядок у таблиці Teachers.
 */
public record SchoolTeacherOptionResponse(
        String id,
        String displayName,
        String email,
        String phone,
        List<String> subjectTitles,
        /** {@code school_subjects.id} for each assigned subject (same order as titles when resolved). */
        List<String> subjectIds,
        List<String> groupNames,
        boolean inviteEmailSent
) {
    public SchoolTeacherOptionResponse(
            String id,
            String displayName,
            String email,
            String phone,
            List<String> subjectTitles,
            List<String> subjectIds,
            List<String> groupNames
    ) {
        this(id, displayName, email, phone, subjectTitles, subjectIds, groupNames, false);
    }
}
