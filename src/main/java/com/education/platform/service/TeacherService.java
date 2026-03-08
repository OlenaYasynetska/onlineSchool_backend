package com.education.platform.service;

import com.education.platform.dto.request.TeacherRequest;
import com.education.platform.dto.response.TeacherResponse;
import com.education.platform.exception.ResourceNotFoundException;
import com.education.platform.mapper.TeacherMapper;
import com.education.platform.model.Teacher;
import com.education.platform.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    public TeacherResponse create(TeacherRequest request) {
        if (teacherRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Teacher profile already exists for this user");
        }
        Teacher teacher = teacherMapper.toEntity(request);
        teacher.setCreatedAt(Instant.now());
        teacher.setUpdatedAt(Instant.now());
        teacher.setCourseIds(teacher.getCourseIds() != null ? teacher.getCourseIds() : Set.of());
        teacher = teacherRepository.save(teacher);
        return teacherMapper.toResponse(teacher);
    }

    public TeacherResponse getById(String id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", id));
        return teacherMapper.toResponse(teacher);
    }

    public TeacherResponse getByUserId(String userId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId=" + userId));
        return teacherMapper.toResponse(teacher);
    }

    public List<TeacherResponse> getBySchoolId(String schoolId) {
        return teacherRepository.findBySchoolId(schoolId).stream()
                .map(teacherMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TeacherResponse update(String id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", id));
        teacherMapper.updateEntity(request, teacher);
        teacher.setUpdatedAt(Instant.now());
        teacher = teacherRepository.save(teacher);
        return teacherMapper.toResponse(teacher);
    }

    public void delete(String id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher", id);
        }
        teacherRepository.deleteById(id);
    }
}
