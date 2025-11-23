package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.UserRepository;
import com.my.repository.impl.InMemoryUserRepositoryImpl;
import com.my.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepositoryImpl();
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testRegistrationSuccess() {
        User registeredUser = userService.registration("new@example.com", "New User", "password");

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getId()).isNotNull();
        assertThat(registeredUser.getEmail()).isEqualTo("new@example.com");
        assertThat(registeredUser.getUsername()).isEqualTo("New User");
        assertThat(registeredUser.getRole()).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    void testRegistrationEmailTaken() {
        assertThatThrownBy(() -> userService.registration("admin@my.ru", "password", "New User"))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("admin@my.ru уже используется");
    }

    @Test
    void testLoginSuccess() {
        User loggedInUser = userService.login("admin@my.ru", "aDmIn");

        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getEmail()).isEqualTo("admin@my.ru");
        assertThat(loggedInUser.getUsername()).isEqualTo("Administrator");
    }

    @Test
    void testLoginWrongPassword() {
        assertThatThrownBy(() -> userService.login("admin@my.ru", "wrongpassword"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Введены неверные данные");
    }

    @Test
    void testLoginWrongEmail() {
        assertThatThrownBy(() -> userService.login("wrong@example.com", "aDmIn"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Введены неверные данные");
    }

    @Test
    void testGetAll() {
        List<User> users = userService.getAll();

        assertThat(users).isNotNull().hasSize(2);
    }

    @Test
    void testGetById() {
        User user = userService.getById(1L);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void testGetByIdNotFound() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь с id 999 не найден");
    }

    @Test
    void testUpdateSuccess() {
        User sourceUser = new User();
        sourceUser.setUsername("Updated Name");

        User updatedUser = userService.update(1L, sourceUser);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("Updated Name");
    }

    @Test
    void testUpdateEmailTaken() {
        User sourceUser = new User();
        sourceUser.setEmail("user@my.ru");

        assertThatThrownBy(() -> userService.update(1L, sourceUser))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("user@my.ru уже используется");
    }

    @Test
    void testUpdateSameEmail() {
        User existingUser = userService.getById(1L);
        User sourceUser = new User();
        sourceUser.setEmail(existingUser.getEmail());

        User updatedUser = userService.update(1L, sourceUser);

        assertThat(updatedUser).isNotNull();
    }

    @Test
    void testUpdateNotFound() {
        User sourceUser = new User();
        sourceUser.setUsername("New Name");

        assertThatThrownBy(() -> userService.update(999L, sourceUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь с id 999 не найден");
    }

    @Test
    void testDelete() {
        boolean deleted = userService.delete(1L);

        assertThat(deleted).isTrue();

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь с id 1 не найден");
    }

    @Test
    void testDeleteNotFound() {
        boolean deleted = userService.delete(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void testIsEmailAvailable() {
        boolean available = userService.isEmailAvailable("new@example.com");

        assertThat(available).isTrue();
    }

    @Test
    void testIsEmailAvailableTaken() {
        boolean available = userService.isEmailAvailable("admin@my.ru");

        assertThat(available).isFalse();
    }
}
