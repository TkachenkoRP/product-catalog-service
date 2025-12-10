package com.my.repository.impl;

import com.my.InstancioTestEntityFactory;
import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.AbstractPostgresqlRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционные тесты репозитория пользователей")
class PostgresqlUserRepositoryImplTest extends AbstractPostgresqlRepositoryTest {
    private final PostgresqlUserRepositoryImpl userRepository;

    @Autowired
    public PostgresqlUserRepositoryImplTest(PostgresqlUserRepositoryImpl userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("findAll() - Получение всех пользователей")
    void whenFindAllUsers_thenReturnAllUsers() {
        List<User> users = userRepository.findAll();

        assertThat(users)
                .isNotNull()
                .hasSize(2)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("testadmin@test.ru", "testuser@test.ru");
    }

    @Test
    @DisplayName("findById() - Получение пользователя по существующему ID")
    void whenFindUserById_withExistingId_thenReturnUser() {
        List<User> allUsers = userRepository.findAll();
        Long existingUserId = allUsers.get(0).getId();

        Optional<User> userOpt = userRepository.findById(existingUserId);

        assertThat(userOpt).isPresent();
        userOpt.ifPresent(user -> {
            assertThat(user.getId()).isEqualTo(existingUserId);
            assertThat(user.getEmail()).isNotBlank();
            assertThat(user.getUsername()).isNotBlank();
            assertThat(user.getRole()).isNotNull();
        });
    }

    @Test
    @DisplayName("findById() - Поиск несуществующего пользователя")
    void whenFindUserById_withNonExistingId_thenReturnEmpty() {
        Optional<User> userOpt = userRepository.findById(999L);
        assertThat(userOpt).isEmpty();
    }

    @Test
    @DisplayName("save() - Сохранение нового пользователя")
    void whenSaveNewUser_thenUserIsPersisted() {
        User newUser = new User("newuser@test.ru", "New User", "password123", UserRole.ROLE_USER);

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser)
                .isNotNull()
                .extracting(User::getId, User::getEmail, User::getUsername, User::getRole)
                .containsExactly(savedUser.getId(), "newuser@test.ru", "New User", UserRole.ROLE_USER);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
    }

    @Test
    @DisplayName("update() - Обновление существующего пользователя")
    void whenUpdateExistingUser_thenUserIsUpdated() {
        List<User> users = userRepository.findAll();
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

        Optional<User> verifiedUser = userRepository.findById(existingUser.getId());
        assertThat(verifiedUser)
                .isPresent()
                .get()
                .extracting(User::getEmail, User::getUsername)
                .containsExactly("updated@test.ru", "Updated User");
    }

    @Test
    @DisplayName("update() - Обновление несуществующего пользователя")
    void whenUpdateUser_withNonExistingId_thenThrowException() {
        User nonExistentUser = new User(999L, "none@test.ru", "None", "pass", UserRole.ROLE_USER);

        try {
            userRepository.update(nonExistentUser);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Ошибка поиска пользователя по ID: 999");
        }
    }

    @Test
    @DisplayName("deleteById() - Удаление пользователя по ID")
    void whenDeleteUserById_withExistingId_thenUserIsDeleted() {
        User userToDelete = new User("todelete@test.ru", "To Delete", "pass123", UserRole.ROLE_USER);
        User savedUser = userRepository.save(userToDelete);
        Long userId = savedUser.getId();

        boolean deleted = userRepository.deleteById(userId);

        assertThat(deleted).isTrue();

        Optional<User> foundUser = userRepository.findById(userId);
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("deleteById() - Удаление несуществующего пользователя")
    void whenDeleteUserById_withNonExistingId_thenReturnFalse() {
        boolean deleted = userRepository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("isPresentByEmail() - Проверка существующего email")
    void whenCheckEmailExists_withExistingEmail_thenReturnTrue() {
        boolean exists = userRepository.isPresentByEmail("testadmin@test.ru");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("isPresentByEmail() - Проверка несуществующего email")
    void whenCheckEmailExists_withNonExistingEmail_thenReturnFalse() {
        boolean exists = userRepository.isPresentByEmail("nonexistent@test.ru");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("getByEmailAndPassword() - Поиск пользователя с валидными учетными данными")
    void whenGetUserByEmailAndPassword_withValidCredentials_thenReturnUser() {
        Optional<User> userOpt = userRepository.getByEmailAndPassword("testadmin@test.ru", "admin123");

        assertThat(userOpt)
                .isPresent()
                .get()
                .extracting(User::getEmail, User::getUsername)
                .containsExactly("testadmin@test.ru", "Test Administrator");
    }

    @Test
    @DisplayName("getByEmailAndPassword() - Поиск пользователя с неверным паролем")
    void whenGetUserByEmailAndPassword_withWrongPassword_thenReturnEmpty() {
        Optional<User> userOpt = userRepository.getByEmailAndPassword("testadmin@test.ru", "wrongpassword");

        assertThat(userOpt).isEmpty();
    }

    @Test
    @DisplayName("getByEmailAndPassword() - Поиск пользователя с неверным email")
    void whenGetUserByEmailAndPassword_withWrongEmail_thenReturnEmpty() {
        Optional<User> userOpt = userRepository.getByEmailAndPassword("wrong@test.ru", "admin123");

        assertThat(userOpt).isEmpty();
    }

    @Test
    @DisplayName("save() - Сохранение пользователя с дублирующимся email")
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
    @DisplayName("findByRole() - Поиск пользователей с ролью ADMIN")
    void whenFindByRole_withAdminRole_thenReturnAllAdmins() {
        User admin1 = InstancioTestEntityFactory.createAdminUser();
        User admin2 = InstancioTestEntityFactory.createAdminUser();
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
    @DisplayName("findByRole() - Поиск пользователей с ролью USER")
    void whenFindByRole_withUserRole_thenReturnAllUsers() {
        User user1 = InstancioTestEntityFactory.createRegularUser();
        User user2 = InstancioTestEntityFactory.createRegularUser();
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