package com.education.web.student;

import com.education.application.student.CreateStudentCommand;
import com.education.application.student.CreateStudentUseCase;
import com.education.application.student.GetStudentByIdUseCase;
import com.education.application.student.GetStudentsBySchoolUseCase;
import com.education.application.student.StudentView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final CreateStudentUseCase createStudentUseCase;
    private final GetStudentByIdUseCase getStudentByIdUseCase;
    private final GetStudentsBySchoolUseCase getStudentsBySchoolUseCase;

    public StudentController(
            CreateStudentUseCase createStudentUseCase,
            GetStudentByIdUseCase getStudentByIdUseCase,
            GetStudentsBySchoolUseCase getStudentsBySchoolUseCase
    ) {
        this.createStudentUseCase = createStudentUseCase;
        this.getStudentByIdUseCase = getStudentByIdUseCase;
        this.getStudentsBySchoolUseCase = getStudentsBySchoolUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentView create(@Valid @RequestBody CreateStudentRequest request) {
        return createStudentUseCase.execute(
                new CreateStudentCommand(request.fullName(), request.email(), request.schoolId())
        );
    }

    @GetMapping("/{studentId}")
    public StudentView getById(@PathVariable String studentId) {
        return getStudentByIdUseCase.execute(studentId);
    }

    @GetMapping
    public List<StudentView> getBySchool(@RequestParam String schoolId) {
        return getStudentsBySchoolUseCase.execute(schoolId);
    }
}

