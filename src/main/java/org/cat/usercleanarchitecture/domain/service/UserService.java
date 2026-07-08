package org.cat.usercleanarchitecture.domain.service;

import org.cat.usercleanarchitecture.domain.model.User;

import java.util.List;

public class UserService {
    public static List<User> findByLastName(List<User> users, String lastName) {
        if (users == null || lastName == null || lastName.isBlank()) return List.of();

        String lastNameToFind = lastName.trim();
        return users.stream()
                .filter(user -> user != null && user.getLastName() != null)
                .filter(user -> user.getLastName().equalsIgnoreCase(lastNameToFind))
                .toList();
    }
}
