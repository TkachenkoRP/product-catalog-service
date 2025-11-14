package com.my.service.impl;

import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.UserRepository;
import com.my.repository.impl.InMemoryUserRepositoryImpl;
import com.my.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        User registeredUser = userService.registration("admin@my.ru", "password", "New User");

        assertThat(registeredUser).isNull();
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
        User loggedInUser = userService.login("admin@my.ru", "wrongpassword");

        assertThat(loggedInUser).isNull();
    }

    @Test
    void testLoginWrongEmail() {
        User loggedInUser = userService.login("wrong@example.com", "aDmIn");

        assertThat(loggedInUser).isNull();
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
        User user = userService.getById(999L);

        assertThat(user).isNull();
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

        User updatedUser = userService.update(1L, sourceUser);

        assertThat(updatedUser).isNull();
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

        User updatedUser = userService.update(999L, sourceUser);

        assertThat(updatedUser).isNull();
    }

    @Test
    void testDelete() {
        boolean deleted = userService.delete(1L);

        assertThat(deleted).isTrue();

        User user = userService.getById(1L);
        assertThat(user).isNull();
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
