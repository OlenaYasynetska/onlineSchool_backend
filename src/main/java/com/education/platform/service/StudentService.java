package com.education.platform.service;

import com.education.platform.dto.request.StudentRequest;
import com.education.platform.dto.response.StudentResponse;
import com.education.platform.exception.ResourceNotFoundException;
import com.education.platform.mapper.StudentMapper;
import com.education.platform.model.Student;
import com.education.platform.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Student profile already exists for this user");
        }
        Student student = studentMapper.toEntity(request);
        student.setCreatedAt(Instant.now());
        student.setUpdatedAt(Instant.now());
        student.setCourseIds(student.getCourseIds() != null ? student.getCourseIds() : Set.of());
        student = studentRepository.save(student);
        return studentMapper.toResponse(student);
    }

    public StudentResponse getById(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        return studentMapper.toResponse(student);
    }

    public StudentResponse getByUserId(String userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId=" + userId));
        return studentMapper.toResponse(student);
    }

    public List<StudentResponse> getBySchoolId(String schoolId) {
        return studentRepository.findBySchoolId(schoolId).stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public StudentResponse update(String id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        studentMapper.updateEntity(request, student);
        student.setUpdatedAt(Instant.now());
        student = studentRepository.save(student);
        return studentMapper.toResponse(student);
    }

    public void delete(String id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", id);
        }
        studentRepository.deleteById(id);
    }
}
