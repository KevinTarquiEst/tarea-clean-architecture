package org.cat.usercleanarchitecture.aplication.ports.output;

import org.cat.usercleanarchitecture.domain.model.User;

import java.util.List;

public interface UserPort {
    User create(User user);

    List<User> findAll();
}
