package com.education.web.schooladmin.service;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupStudentEntity;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SchoolAdminStudentGroupService {

    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolGroupStudentJpaRepository enrollments;
    private final SpringDataStudentJpaRepository students;

    public SchoolAdminStudentGroupService(
            SchoolGroupJpaRepository schoolGroups,
            SchoolGroupStudentJpaRepository enrollments,
            SpringDataStudentJpaRepository students
    ) {
        this.schoolGroups = schoolGroups;
        this.enrollments = enrollments;
        this.students = students;
    }

    /**
     * Додає вже створеного студента до існуючої групи; оновлює {@code students_count} у групі.
     */
    @Transactional
    public void enrollStudent(String schoolId, String groupId, String studentId) {
        SchoolGroupEntity group = schoolGroups.findById(groupId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found")
        );
        if (!group.getOrganization().getId().equals(schoolId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Group does not belong to this school"
            );
        }
        StudentJpaEntity student = students.findById(studentId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found")
        );
        if (!student.getSchoolId().equals(schoolId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Student does not belong to this school"
            );
        }
        if (enrollments.existsByStudentIdAndGroup_Id(studentId, groupId)) {
            return;
        }
        SchoolGroupStudentEntity row = new SchoolGroupStudentEntity();
        row.setStudentId(studentId);
        row.setGroup(group);
        row.setChangeDelta(1);
        enrollments.save(row);
        group.setStudentsCount(group.getStudentsCount() + 1);
        schoolGroups.save(group);
    }
}
