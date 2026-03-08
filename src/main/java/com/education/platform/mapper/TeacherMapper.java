package com.education.platform.mapper;

import com.education.platform.dto.request.TeacherRequest;
import com.education.platform.dto.response.TeacherResponse;
import com.education.platform.model.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    TeacherResponse toResponse(Teacher teacher);

    Teacher toEntity(TeacherRequest request);

    void updateEntity(TeacherRequest request, @MappingTarget Teacher teacher);
}
