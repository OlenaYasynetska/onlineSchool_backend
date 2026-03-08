package com.education.platform.controller;

import com.education.platform.dto.request.TeacherRequest;
import com.education.platform.dto.response.TeacherResponse;
import com.education.platform.service.TeacherService;
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
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Teacher management")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_SCHOOL')")
    @Operation(summary = "Create teacher")
    public TeacherResponse create(@Valid @RequestBody TeacherRequest request) {
        return teacherService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by id")
    public TeacherResponse getById(@PathVariable String id) {
        return teacherService.getById(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get teacher by user id")
    public TeacherResponse getByUserId(@PathVariable String userId) {
        return teacherService.getByUserId(userId);
    }

    @GetMapping
    @Operation(summary = "Get teachers by school")
    public List<TeacherResponse> getBySchoolId(@RequestParam String schoolId) {
        return teacherService.getBySchoolId(schoolId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_SCHOOL')")
    @Operation(summary = "Update teacher")
    public TeacherResponse update(@PathVariable String id, @Valid @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_SCHOOL')")
    @Operation(summary = "Delete teacher")
    public void delete(@PathVariable String id) {
        teacherService.delete(id);
    }
}
