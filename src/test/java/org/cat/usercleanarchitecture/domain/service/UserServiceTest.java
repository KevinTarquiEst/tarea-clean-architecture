package org.cat.usercleanarchitecture.domain.service;

import org.cat.usercleanarchitecture.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private User user(String firstName, String email) {
        return new User(firstName, "Doe", email, "0000000");
    }

    @Test
    void findsUsersByFullDomain() {
        List<User> users = List.of(
                user("Ana", "ana@gmail.com"),
                user("Luis", "luis@hotmail.com"),
                user("Carlos", "carlos@gmail.com")
        );

        List<User> result = UserService.findByEmailDomain(users, "gmail.com");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getFirstName().equals("Ana")));
        assertTrue(result.stream().anyMatch(u -> u.getFirstName().equals("Carlos")));
    }

    @Test
    void findsUsersByProviderWithoutTld() {
        List<User> users = List.of(user("Ana", "ana@gmail.com"));

        List<User> result = UserService.findByEmailDomain(users, "gmail");

        assertEquals(1, result.size());
    }

    @Test
    void normalizesLeadingAtSign() {
        List<User> users = List.of(user("Ana", "ana@gmail.com"));

        List<User> result = UserService.findByEmailDomain(users, "@gmail.com");

        assertEquals(1, result.size());
    }

    @Test
    void isCaseInsensitive() {
        List<User> users = List.of(user("Maria", "Maria@GMAIL.com"));

        List<User> result = UserService.findByEmailDomain(users, "gmail.com");

        assertEquals(1, result.size());
    }

    @Test
    void returnsEmptyListWhenNoMatches() {
        List<User> users = List.of(user("Ana", "ana@gmail.com"));

        List<User> result = UserService.findByEmailDomain(users, "yahoo.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void excludesUsersWithNullEmail() {
        List<User> users = List.of(new User("Ana", "Doe", null, "0000000"));

        List<User> result = UserService.findByEmailDomain(users, "gmail.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyListWhenUsersListIsNull() {
        List<User> result = UserService.findByEmailDomain(null, "gmail.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyListWhenFilterIsBlank() {
        List<User> users = List.of(user("Ana", "ana@gmail.com"));

        List<User> result = UserService.findByEmailDomain(users, "   ");

        assertTrue(result.isEmpty());
    }
}
