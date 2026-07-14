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

    public static List<User> findByEmailDomain(List<User> users, String domainFilter) {
        if (users == null || domainFilter == null || domainFilter.isBlank()) return List.of();

        String normalizedFilter = domainFilter.trim();
        if (normalizedFilter.startsWith("@")) {
            normalizedFilter = normalizedFilter.substring(1);
        }
        normalizedFilter = normalizedFilter.toLowerCase();
        String finalFilter = normalizedFilter;

        return users.stream()
                .filter(user -> user != null && user.getEmail() != null)
                .filter(user -> user.getEmail().toLowerCase().contains(finalFilter))
                .toList();
    }
}
