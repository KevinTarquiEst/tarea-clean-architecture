package org.cat.usercleanarchitecture.infraestructure.adapters.output.mapper;

import org.cat.usercleanarchitecture.domain.model.User;
import org.cat.usercleanarchitecture.infraestructure.adapters.output.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserEntityMapper {

    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);

    UserEntity toUserEntity(User user);
    User toUser(UserEntity userEntity);
}
