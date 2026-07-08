package org.cat.usercleanarchitecture.infraestructure.adapters.input.mapper;

import org.cat.usercleanarchitecture.domain.model.User;
import org.cat.usercleanarchitecture.infraestructure.adapters.input.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserResponseMapper {

    UserResponseMapper INSTANCE = Mappers.getMapper(UserResponseMapper.class);

    UserResponse toUserRequest(User user);
    User toUser(UserResponse userResponse);
}
