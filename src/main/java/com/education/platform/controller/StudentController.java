package com.education.platform.controller;

import com.education.platform.dto.request.StudentRequest;
import com.education.platform.dto.response.StudentResponse;
import com.education.platform.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student management")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_SCHOOL')")
    @Operation(summary = "Create student")
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return studentService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by id")
    public StudentResponse getById(@PathVariable String id) {
        return studentService.getById(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get student by user id")
    public StudentResponse getByUserId(@PathVariable String userId) {
        return studentService.getByUserId(userId);
    }

    @GetMapping
    @Operation(summary = "Get students by school")
    public List<StudentResponse> getBySchoolId(@RequestParam String schoolId) {
        return studentService.getBySchoolId(schoolId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_SCHOOL')")
    @Operation(summary = "Update student")
    public StudentResponse update(@PathVariable String id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_SCHOOL')")
    @Operation(summary = "Delete student")
    public void delete(@PathVariable String id) {
        studentService.delete(id);
    }
}
