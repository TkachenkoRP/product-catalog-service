package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.UserRepository;
import com.my.security.UserManager;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper);
        UserManager.setLoggedInUser(null);
    }

    @AfterEach
    void tearDown() {
        UserManager.setLoggedInUser(null);
    }

    @Test
    void whenRegistrationWithAvailableEmail_thenReturnSavedUserAndSetLoggedIn() {
        String email = "newuser@test.ru";
        String password = "password123";
        String name = "New User";
        User savedUser = new User(1L, email, name, password, UserRole.ROLE_USER);

        when(userRepository.isPresentByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registration(email, password, name);

        assertThat(result).isEqualTo(savedUser);
        assertThat(UserManager.getLoggedInUser()).isEqualTo(savedUser);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenRegistrationWithExistingEmail_thenThrowException() {
        String email = "existing@test.ru";
        String password = "password123";
        String name = "Existing User";

        when(userRepository.isPresentByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userService.registration(email, password, name))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining(email + " уже используется");

        verify(userRepository, never()).save(any());
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnUserAndSetLoggedIn() {
        String email = "test@test.ru";
        String password = "password123";
        User user = new User(1L, email, "Test User", password, UserRole.ROLE_USER);

        when(userRepository.getByEmailAndPassword(email, password)).thenReturn(Optional.of(user));

        User result = userService.login(email, password);

        assertThat(result).isEqualTo(user);
        assertThat(UserManager.getLoggedInUser()).isEqualTo(user);
    }

    @Test
    void whenLoginWithInvalidCredentials_thenThrowException() {
        String email = "test@test.ru";
        String password = "wrongpassword";

        when(userRepository.getByEmailAndPassword(email, password)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(email, password))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Введены неверные данные");

        assertThat(UserManager.getLoggedInUser()).isNull();
    }

    @Test
    void whenLogout_thenClearLoggedInUser() {
        User loggedInUser = new User(1L, "test@test.ru", "Test User", "password", UserRole.ROLE_USER);
        UserManager.setLoggedInUser(loggedInUser);

        userService.logout();

        assertThat(UserManager.getLoggedInUser()).isNull();
    }

    @Test
    void whenGetAllUsers_thenReturnAllUsers() {
        List<User> expectedUsers = Instancio.ofList(User.class).create();
        when(userRepository.getAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAll();

        assertThat(result).isEqualTo(expectedUsers);
        verify(userRepository).getAll();
    }

    @Test
    void whenGetExistingUserById_thenReturnUser() {
        Long userId = 1L;
        User expectedUser = new User(userId, "test@test.ru", "Test User", "password", UserRole.ROLE_USER);
        when(userRepository.getById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getById(userId);

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    void whenGetNonExistingUserById_thenThrowException() {
        Long userId = 999L;
        when(userRepository.getById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь с id " + userId + " не найден");
    }

    @Test
    void whenUpdateUserWithAvailableEmail_thenReturnUpdatedUser() {
        Long userId = 1L;
        User sourceUser = new User("updated@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        User existingUser = new User(userId, "old@test.ru", "Old User", "oldpassword", UserRole.ROLE_USER);
        User updatedUser = new User(userId, "updated@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);

        when(userRepository.getById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.isPresentByEmail("updated@test.ru")).thenReturn(false);
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        User result = userService.update(userId, sourceUser);

        assertThat(result).isEqualTo(updatedUser);
        verify(userMapper).updateUser(sourceUser, existingUser);
        verify(userRepository).update(existingUser);
    }

    @Test
    void whenUpdateUserWithSameEmail_thenReturnUpdatedUser() {
        Long userId = 1L;
        User sourceUser = new User("same@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        User existingUser = new User(userId, "same@test.ru", "Old User", "oldpassword", UserRole.ROLE_USER);
        User updatedUser = new User(userId, "same@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);

        when(userRepository.getById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        User result = userService.update(userId, sourceUser);

        assertThat(result).isEqualTo(updatedUser);
        verify(userRepository, never()).isPresentByEmail(anyString());
    }

    @Test
    void whenUpdateUserWithExistingEmail_thenThrowException() {
        Long userId = 1L;
        User sourceUser = new User("existing@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        User existingUser = new User(userId, "old@test.ru", "Old User", "oldpassword", UserRole.ROLE_USER);

        when(userRepository.getById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.isPresentByEmail("existing@test.ru")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(userId, sourceUser))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("existing@test.ru уже используется");

        verify(userRepository, never()).update(any());
    }

    @Test
    void whenDeleteUser_thenReturnResultFromRepository() {
        Long userId = 1L;
        when(userRepository.deleteById(userId)).thenReturn(true);

        boolean result = userService.delete(userId);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(userId);
    }

    @Test
    void whenCheckEmailAvailable_thenReturnTrue() {
        String email = "available@test.ru";
        when(userRepository.isPresentByEmail(email)).thenReturn(false);

        boolean result = userService.isEmailAvailable(email);

        assertThat(result).isTrue();
    }

    @Test
    void whenCheckEmailNotAvailable_thenReturnFalse() {
        String email = "taken@test.ru";
        when(userRepository.isPresentByEmail(email)).thenReturn(true);

        boolean result = userService.isEmailAvailable(email);

        assertThat(result).isFalse();
    }
}
