package com.my.repository.impl;

import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.AbstractPostgresqlRepositoryTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class PostgresqlUserRepositoryImplTest extends AbstractPostgresqlRepositoryTest {
    private final PostgresqlUserRepositoryImpl userRepository;

    @Autowired
    public PostgresqlUserRepositoryImplTest(PostgresqlUserRepositoryImpl userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    void whenGetAllUsers_thenReturnAllUsers() {
        List<User> users = userRepository.getAll();

        assertThat(users)
                .isNotNull()
                .hasSize(2)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("testadmin@test.ru", "testuser@test.ru");
    }

    @Test
    void whenGetUserById_withExistingId_thenReturnUser() {
        List<User> allUsers = userRepository.getAll();
        Long existingUserId = allUsers.get(0).getId();

        Optional<User> userOpt = userRepository.getById(existingUserId);

        assertThat(userOpt).isPresent();
        userOpt.ifPresent(user -> {
            assertThat(user.getId()).isEqualTo(existingUserId);
            assertThat(user.getEmail()).isNotBlank();
            assertThat(user.getUsername()).isNotBlank();
            assertThat(user.getRole()).isNotNull();
        });
    }

    @Test
    void whenGetUserById_withNonExistingId_thenReturnEmpty() {
        Optional<User> userOpt = userRepository.getById(999L);
        assertThat(userOpt).isEmpty();
    }

    @Test
    void whenSaveNewUser_thenUserIsPersisted() {
        User newUser = new User("newuser@test.ru", "New User", "password123", UserRole.ROLE_USER);

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser)
                .isNotNull()
                .extracting(User::getId, User::getEmail, User::getUsername, User::getRole)
                .containsExactly(savedUser.getId(), "newuser@test.ru", "New User", UserRole.ROLE_USER);

        Optional<User> foundUser = userRepository.getById(savedUser.getId());
        assertThat(foundUser).isPresent();
    }

    @Test
    void whenUpdateExistingUser_thenUserIsUpdated() {
        List<User> users = userRepository.getAll();
        User existingUser = users.get(0);

        User userToUpdate = new User(
                existingUser.getId(),
                "updated@test.ru",
                "Updated User",
                "newpassword123",
                UserRole.ROLE_ADMIN
        );

        User updatedUser = userRepository.update(userToUpdate);

        assertThat(updatedUser)
                .isNotNull()
                .extracting(User::getEmail, User::getUsername, User::getRole)
                .containsExactly("updated@test.ru", "Updated User", UserRole.ROLE_ADMIN);

        Optional<User> verifiedUser = userRepository.getById(existingUser.getId());
        assertThat(verifiedUser)
                .isPresent()
                .get()
                .extracting(User::getEmail, User::getUsername)
                .containsExactly("updated@test.ru", "Updated User");
    }

    @Test
    void whenUpdateUser_withNonExistingId_thenThrowException() {
        User nonExistentUser = new User(999L, "none@test.ru", "None", "pass", UserRole.ROLE_USER);

        try {
            userRepository.update(nonExistentUser);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Ошибка поиска пользователя по ID: 999");
        }
    }

    @Test
    void whenDeleteUserById_withExistingId_thenUserIsDeleted() {
        User userToDelete = new User("todelete@test.ru", "To Delete", "pass123", UserRole.ROLE_USER);
        User savedUser = userRepository.save(userToDelete);
        Long userId = savedUser.getId();

        boolean deleted = userRepository.deleteById(userId);

        assertThat(deleted).isTrue();

        Optional<User> foundUser = userRepository.getById(userId);
        assertThat(foundUser).isEmpty();
    }

    @Test
    void whenDeleteUserById_withNonExistingId_thenReturnFalse() {
        boolean deleted = userRepository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void whenCheckEmailExists_withExistingEmail_thenReturnTrue() {
        boolean exists = userRepository.isPresentByEmail("testadmin@test.ru");

        assertThat(exists).isTrue();
    }

    @Test
    void whenCheckEmailExists_withNonExistingEmail_thenReturnFalse() {
        boolean exists = userRepository.isPresentByEmail("nonexistent@test.ru");

        assertThat(exists).isFalse();
    }

    @Test
    void whenGetUserByEmailAndPassword_withValidCredentials_thenReturnUser() {
        Optional<User> userOpt = userRepository.getByEmailAndPassword("testadmin@test.ru", "admin123");

        assertThat(userOpt)
                .isPresent()
                .get()
                .extracting(User::getEmail, User::getUsername)
                .containsExactly("testadmin@test.ru", "Test Administrator");
    }

    @Test
    void whenGetUserByEmailAndPassword_withWrongPassword_thenReturnEmpty() {
        Optional<User> userOpt = userRepository.getByEmailAndPassword("testadmin@test.ru", "wrongpassword");

        assertThat(userOpt).isEmpty();
    }

    @Test
    void whenGetUserByEmailAndPassword_withWrongEmail_thenReturnEmpty() {
        Optional<User> userOpt = userRepository.getByEmailAndPassword("wrong@test.ru", "admin123");

        assertThat(userOpt).isEmpty();
    }

    @Test
    void whenSaveUser_withDuplicateEmail_thenThrowException() {
        User user1 = new User("duplicate@test.ru", "User One", "pass1", UserRole.ROLE_USER);
        userRepository.save(user1);

        User user2 = new User("duplicate@test.ru", "User Two", "pass2", UserRole.ROLE_USER);

        try {
            userRepository.save(user2);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("user_email_key");
        }
    }

    @Test
    void whenFindByRole_withAdminRole_thenReturnAllAdmins() {
        User admin1 = Instancio.of(User.class)
                .ignore(field(User::getId))
                .set(field(User::getRole), UserRole.ROLE_ADMIN)
                .create();
        User admin2 = Instancio.of(User.class)
                .ignore(field(User::getId))
                .set(field(User::getRole), UserRole.ROLE_ADMIN)
                .create();
        userRepository.save(admin1);
        userRepository.save(admin2);

        List<User> admins = userRepository.findByRole(UserRole.ROLE_ADMIN);

        assertThat(admins)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .extracting(User::getRole)
                .containsOnly(UserRole.ROLE_ADMIN);
    }

    @Test
    void whenFindByRole_withUserRole_thenReturnAllUsers() {
        User user1 = Instancio.of(User.class)
                .ignore(field(User::getId))
                .set(field(User::getRole), UserRole.ROLE_USER)
                .create();
        User user2 = Instancio.of(User.class)
                .ignore(field(User::getId))
                .set(field(User::getRole), UserRole.ROLE_USER)
                .create();
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findByRole(UserRole.ROLE_USER);

        assertThat(users)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .extracting(User::getRole)
                .containsOnly(UserRole.ROLE_USER);
    }
}