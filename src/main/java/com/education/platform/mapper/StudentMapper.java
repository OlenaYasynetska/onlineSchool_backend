package com.education.platform.mapper;

import com.education.platform.dto.request.StudentRequest;
import com.education.platform.dto.response.StudentResponse;
import com.education.platform.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentResponse toResponse(Student student);

    Student toEntity(StudentRequest request);

    void updateEntity(StudentRequest request, @MappingTarget Student student);
}
