package com.education.platform.mapper;

import com.education.platform.dto.response.UserResponse;
import com.education.platform.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    UserResponse toResponse(User user);
}
