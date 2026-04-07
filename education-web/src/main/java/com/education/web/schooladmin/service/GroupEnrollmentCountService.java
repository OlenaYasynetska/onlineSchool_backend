package com.education.web.schooladmin.service;

import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Фактична кількість учнів у групах за записами {@code school_group_students}
 * (не денормалізоване поле {@code school_groups.students_count}).
 */
@Service
public class GroupEnrollmentCountService {

    private final SchoolGroupStudentJpaRepository schoolGroupStudents;

    public GroupEnrollmentCountService(SchoolGroupStudentJpaRepository schoolGroupStudents) {
        this.schoolGroupStudents = schoolGroupStudents;
    }

    public int countForGroup(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return 0;
        }
        long n = schoolGroupStudents.countByGroup_Id(groupId.trim());
        return (int) Math.min(n, Integer.MAX_VALUE);
    }

    public Map<String, Long> countsByGroupIds(Collection<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = groupIds.stream().filter(id -> id != null && !id.isBlank()).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = schoolGroupStudents.countRowsByGroupIds(ids);
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row.length >= 2 && row[0] != null && row[1] != null) {
                map.put((String) row[0], ((Number) row[1]).longValue());
            }
        }
        return map;
    }
}
