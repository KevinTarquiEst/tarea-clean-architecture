package org.cat.usercleanarchitecture.aplication.ports.input;

import org.cat.usercleanarchitecture.domain.model.User;

import java.util.List;

public interface IUserUseCase {
    User create(User user);

    List<User> findByLastName(String lastName);

    List<User> findByEmailDomain(String email);
}
