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
        List<String> groupNames,
        boolean inviteEmailSent
) {
    public SchoolTeacherOptionResponse(
            String id,
            String displayName,
            String email,
            String phone,
            List<String> subjectTitles,
            List<String> groupNames
    ) {
        this(id, displayName, email, phone, subjectTitles, groupNames, false);
    }
}
