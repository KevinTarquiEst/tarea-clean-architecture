package org.cat.usercleanarchitecture.infraestructure.adapters.input.mapper;

import org.cat.usercleanarchitecture.domain.model.User;
import org.cat.usercleanarchitecture.infraestructure.adapters.input.dto.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserRequestMapper {

    UserRequestMapper INSTANCE = Mappers.getMapper(UserRequestMapper.class);

    UserRequest toUserRequest(User user);
    User toUser(UserRequest userRequest);
}
