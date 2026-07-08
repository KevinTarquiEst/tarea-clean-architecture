package org.cat.usercleanarchitecture.aplication.ports.output;

import org.cat.usercleanarchitecture.domain.model.User;

public interface UserPort {
    User create(User user);
}
