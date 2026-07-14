package org.cat.usercleanarchitecture.aplication.usecases;

import org.cat.usercleanarchitecture.aplication.ports.output.UserPort;
import org.cat.usercleanarchitecture.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserUseCaseImplTest {

    private static class FakeUserPort implements UserPort {
        private final List<User> users;

        FakeUserPort(List<User> users) {
            this.users = users;
        }

        @Override
        public User create(User user) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public List<User> findAll() {
            return users;
        }
    }

    @Test
    void delegatesToUserServiceForValidEmailFilter() {
        List<User> users = List.of(
                new User("Ana", "Doe", "ana@gmail.com", "0000000"),
                new User("Luis", "Doe", "luis@hotmail.com", "0000000")
        );
        UserUseCaseImpl useCase = new UserUseCaseImpl(new FakeUserPort(users));

        List<User> result = useCase.findByEmailDomain("gmail.com");

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getFirstName());
    }

    @Test
    void throwsWhenEmailFilterIsBlank() {
        UserUseCaseImpl useCase = new UserUseCaseImpl(new FakeUserPort(List.of()));

        assertThrows(IllegalArgumentException.class, () -> useCase.findByEmailDomain("   "));
    }

    @Test
    void throwsWhenEmailFilterIsNull() {
        UserUseCaseImpl useCase = new UserUseCaseImpl(new FakeUserPort(List.of()));

        assertThrows(IllegalArgumentException.class, () -> useCase.findByEmailDomain(null));
    }
}
