package com.my.repository.impl;

import com.my.model.User;
import com.my.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryUserRepositoryImplTest {
    private InMemoryUserRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepositoryImpl();
    }

    @Test
    void testRepositoryInitialization() {
        List<User> users = repository.getAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("admin@my.ru", "user@my.ru");
    }

    @Test
    void testGetAll() {
        List<User> users = repository.getAll();

        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        assertThat(users).allSatisfy(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getEmail()).isNotBlank();
            assertThat(user.getUsername()).isNotBlank();
            assertThat(user.getRole()).isNotNull();
        });
    }

    @Test
    void testGetById() {
        Optional<User> userOpt = repository.getById(1L);

        assertThat(userOpt).isPresent();
        userOpt.ifPresent(user -> {
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getEmail()).isNotBlank();
        });
    }

    @Test
    void testGetByIdNotFound() {
        Optional<User> userOpt = repository.getById(999L);

        assertThat(userOpt).isEmpty();
    }

    @Test
    void testSave() {
        User newUser = new User("new@example.com", "newuser", "password", UserRole.ROLE_USER);

        User savedUser = repository.save(newUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");

        List<User> users = repository.getAll();
        assertThat(users).hasSize(3);
    }

    @Test
    void testUpdate() {
        User updatedUser = new User("updated@example.com", "updateduser", "newpass", UserRole.ROLE_ADMIN);
        updatedUser.setId(1L);

        User result = repository.update(updatedUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    void testUpdateNotFound() {
        User nonExistentUser = new User("none@example.com", "none", "pass", UserRole.ROLE_USER);
        nonExistentUser.setId(999L);

        User result = repository.update(nonExistentUser);

        assertThat(result).isNull();
    }

    @Test
    void testDeleteById() {
        boolean deleted = repository.deleteById(1L);

        assertThat(deleted).isTrue();

        Optional<User> userOpt = repository.getById(1L);
        assertThat(userOpt).isEmpty();

        List<User> users = repository.getAll();
        assertThat(users).hasSize(1);
    }

    @Test
    void testDeleteByIdNotFound() {
        boolean deleted = repository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void testIsPresentByEmail() {
        boolean present = repository.isPresentByEmail("admin@my.ru");

        assertThat(present).isTrue();
    }

    @Test
    void testIsPresentByEmailNotFound() {
        boolean present = repository.isPresentByEmail("nonexistent@example.com");

        assertThat(present).isFalse();
    }

    @Test
    void testIsPresentByEmailCaseInsensitive() {
        boolean present = repository.isPresentByEmail("ADMIN@MY.RU");

        assertThat(present).isTrue();
    }

    @Test
    void testGetByEmailAndPassword() {
        Optional<User> userOpt = repository.getByEmailAndPassword("admin@my.ru", "aDmIn");

        assertThat(userOpt).isPresent();
        userOpt.ifPresent(user -> {
            assertThat(user.getEmail()).isEqualTo("admin@my.ru");
            assertThat(user.getUsername()).isEqualTo("Administrator");
        });
    }

    @Test
    void testGetByEmailAndPasswordWrongPassword() {
        Optional<User> userOpt = repository.getByEmailAndPassword("admin@my.ru", "wrongpassword");

        assertThat(userOpt).isEmpty();
    }

    @Test
    void testGetByEmailAndPasswordWrongEmail() {
        Optional<User> userOpt = repository.getByEmailAndPassword("wrong@example.com", "aDmIn");

        assertThat(userOpt).isEmpty();
    }

    @Test
    void testLoadData() {
        List<User> newUsers = List.of(
                new User(10L, "load1@example.com", "loaduser1", "pass1", UserRole.ROLE_USER),
                new User(20L, "load2@example.com", "loaduser2", "pass2", UserRole.ROLE_ADMIN)
        );

        repository.loadData(newUsers);

        List<User> users = repository.getAll();
        assertThat(users).hasSize(4);

        Optional<User> user1 = repository.getById(10L);
        assertThat(user1).isPresent();

        Optional<User> user2 = repository.getById(20L);
        assertThat(user2).isPresent();
    }

    @Test
    void testLoadDataEmptyList() {
        repository.loadData(List.of());

        List<User> users = repository.getAll();
        assertThat(users).isNotEmpty();
    }

    @Test
    void testLoadDataNull() {
        repository.loadData(null);

        List<User> users = repository.getAll();
        assertThat(users).isNotEmpty();
    }
}
