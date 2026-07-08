package org.cat.usercleanarchitecture.infraestructure.adapters.output;

import org.cat.usercleanarchitecture.aplication.ports.output.UserPort;
import org.cat.usercleanarchitecture.domain.model.User;
import org.cat.usercleanarchitecture.infraestructure.adapters.output.mapper.UserEntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAdapter implements UserPort {

    private final UserEntityRepository repository;

    public UserAdapter(UserEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
        UserEntity userEntity = repository.save(UserEntityMapper.INSTANCE.toUserEntity(user));
        return UserEntityMapper.INSTANCE.toUser(userEntity);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(UserEntityMapper.INSTANCE::toUser)
                .toList();
    }
}
