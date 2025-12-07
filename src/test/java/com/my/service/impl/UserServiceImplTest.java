package com.my.service.impl;

import com.my.UserManagerMockHelper;
import com.my.exception.AccessDeniedException;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.exception.LastAdminException;
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
import static org.instancio.Select.field;
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
    void whenGetAllAsAdmin_thenReturnAllUsers() {
        UserManagerMockHelper.setAdminUser();
        List<User> expectedUsers = Instancio.ofList(User.class).create();
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAll();

        assertThat(result).isEqualTo(expectedUsers);
        verify(userRepository).findAll();
    }

    @Test
    void whenGetAllAsRegularUser_thenThrowAccessDeniedException() {
        UserManagerMockHelper.setRegularUser(1L);

        assertThatThrownBy(() -> userService.getAll())
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findAll();
    }

    @Test
    void whenGetAllAsUnauthenticated_thenThrowAccessDeniedException() {
        UserManagerMockHelper.clearUser();

        assertThatThrownBy(() -> userService.getAll())
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findAll();
    }

    @Test
    void whenGetByIdAsAdmin_thenReturnUser() {
        UserManagerMockHelper.setAdminUser();
        Long userId = 1L;
        User expectedUser = new User(userId, "test@test.ru", "Test User", "password", UserRole.ROLE_USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getById(userId);

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    void whenGetByIdAsCurrentUser_thenReturnUser() {
        Long userId = 1L;
        User currentUser = new User(userId, "me@test.ru", "Me", "password", UserRole.ROLE_USER);
        UserManagerMockHelper.setRegularUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

        User result = userService.getById(userId);

        assertThat(result).isEqualTo(currentUser);
        verify(userRepository).findById(userId);
    }

    @Test
    void whenGetByIdAsOtherUser_thenThrowAccessDeniedException() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        UserManagerMockHelper.setRegularUser(currentUserId);

        assertThatThrownBy(() -> userService.getById(targetUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void whenGetNonExistingUserById_thenThrowException() {
        UserManagerMockHelper.setAdminUser();
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь с id " + userId + " не найден");
    }

    @Test
    void whenUpdateAsAdmin_thenReturnUpdatedUser() {
        UserManagerMockHelper.setAdminUser();
        Long userId = 1L;
        User sourceUser = new User("updated@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        User existingUser = new User(userId, "old@test.ru", "Old User", "oldpassword", UserRole.ROLE_USER);
        User updatedUser = new User(userId, "updated@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.isPresentByEmail("updated@test.ru")).thenReturn(false);
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        User result = userService.update(userId, sourceUser);

        assertThat(result).isEqualTo(updatedUser);
        verify(userMapper).updateUser(sourceUser, existingUser);
        verify(userRepository).update(existingUser);
    }

    @Test
    void whenUpdateAsCurrentUser_thenReturnUpdatedUser() {
        Long userId = 1L;
        User currentUser = new User(userId, "me@test.ru", "Me", "password", UserRole.ROLE_USER);
        UserManagerMockHelper.setRegularUser(userId);

        User sourceUser = new User("updated@test.ru", "Updated Me", "newpassword", UserRole.ROLE_USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.isPresentByEmail("updated@test.ru")).thenReturn(false);
        when(userRepository.update(currentUser)).thenReturn(sourceUser);

        User result = userService.update(userId, sourceUser);

        assertThat(result).isEqualTo(sourceUser);
        verify(userMapper).updateUser(sourceUser, currentUser);
    }

    @Test
    void whenUpdateAsOtherUser_thenThrowAccessDeniedException() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        UserManagerMockHelper.setRegularUser(currentUserId);

        User sourceUser = new User("updated@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);

        assertThatThrownBy(() -> userService.update(targetUserId, sourceUser))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findById(any());
        verify(userMapper, never()).updateUser(any(), any());
    }

    @Test
    void whenUpdateUserWithSameEmail_thenReturnUpdatedUser() {
        UserManagerMockHelper.setAdminUser();
        Long userId = 1L;
        User sourceUser = new User("same@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        User existingUser = new User(userId, "same@test.ru", "Old User", "oldpassword", UserRole.ROLE_USER);
        User updatedUser = new User(userId, "same@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.update(existingUser)).thenReturn(updatedUser);

        User result = userService.update(userId, sourceUser);

        assertThat(result).isEqualTo(updatedUser);
        verify(userRepository, never()).isPresentByEmail(anyString());
    }

    @Test
    void whenUpdateUserWithExistingEmail_thenThrowException() {
        UserManagerMockHelper.setAdminUser();
        Long userId = 1L;
        User sourceUser = new User("existing@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        User existingUser = new User(userId, "old@test.ru", "Old User", "oldpassword", UserRole.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.isPresentByEmail("existing@test.ru")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(userId, sourceUser))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("existing@test.ru уже используется");

        verify(userRepository, never()).update(any());
    }

    @Test
    void whenDeleteAsAdmin_thenReturnSuccess() {
        UserManagerMockHelper.setAdminUser();
        Long userId = 1L;
        User userToDelete = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User:: getRole), UserRole.ROLE_USER)
                .create();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        when(userRepository.deleteById(userId)).thenReturn(true);

        boolean result = userService.delete(userId);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(userId);
    }

    @Test
    void whenDeleteAsCurrentUser_thenReturnSuccess() {
        Long userId = 1L;
        User currentUser = new User(userId, "me@test.ru", "Me", "password", UserRole.ROLE_USER);
        UserManagerMockHelper.setRegularUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.deleteById(userId)).thenReturn(true);

        boolean result = userService.delete(userId);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(userId);
    }

    @Test
    void whenDeleteAsOtherUser_thenThrowAccessDeniedException() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        UserManagerMockHelper.setRegularUser(currentUserId);

        assertThatThrownBy(() -> userService.delete(targetUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void whenDeleteLastAdminAsAdmin_thenThrowLastAdminException() {
        Long adminId = 1L;
        User admin = Instancio.of(User.class)
                .set(field(User::getId), adminId)
                .set(field(User::getRole), UserRole.ROLE_ADMIN)
                .create();
        UserManagerMockHelper.setCurrentUser(admin);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(List.of(admin));

        assertThatThrownBy(() -> userService.delete(adminId))
                .isInstanceOf(LastAdminException.class);

        verify(userRepository, never()).deleteById(any());
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

    @Test
    void whenPromoteToAdminAsAdmin_thenReturnPromotedUser() {
        UserManagerMockHelper.setAdminUser();
        Long userId = 2L;
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getRole), UserRole.ROLE_USER)
                .create();
        User promotedUser = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getRole), UserRole.ROLE_ADMIN)
                .create();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.update(user)).thenReturn(promotedUser);

        User result = userService.promoteToAdmin(userId);

        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
        verify(userRepository).update(user);
    }

    @Test
    void whenPromoteToAdminAsRegularUser_thenThrowAccessDeniedException() {
        UserManagerMockHelper.setRegularUser(1L);
        Long userId = 2L;

        assertThatThrownBy(() -> userService.promoteToAdmin(userId))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void whenDemoteFromAdminAsAdmin_thenReturnDemotedUser() {
        Long adminId = 1L;
        User admin = new User(adminId, "admin@test.ru", "Admin", "password", UserRole.ROLE_ADMIN);
        UserManagerMockHelper.setCurrentUser(admin);
        Long targetUserId = 2L;
        User targetAdmin = new User(targetUserId, "target@test.ru", "Target", "password", UserRole.ROLE_ADMIN);
        User demotedUser = new User(targetUserId, "target@test.ru", "Target", "password", UserRole.ROLE_USER);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(List.of(admin, targetAdmin));
        when(userRepository.update(targetAdmin)).thenReturn(demotedUser);

        User result = userService.demoteFromAdmin(targetUserId);

        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_USER);
        verify(userRepository).update(targetAdmin);
    }

    @Test
    void whenDemoteSelfFromAdmin_thenThrowAccessDeniedException() {
        Long adminId = 1L;
        User admin = new User(adminId, "admin@test.ru", "Admin", "password", UserRole.ROLE_ADMIN);
        UserManagerMockHelper.setCurrentUser(admin);

        assertThatThrownBy(() -> userService.demoteFromAdmin(adminId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void whenDemoteLastAdmin_thenThrowLastAdminException() {
        Long adminId = 1L;
        User admin = new User(adminId, "admin@test.ru", "Admin", "password", UserRole.ROLE_ADMIN);
        UserManagerMockHelper.setCurrentUser(admin);
        Long targetUserId = 2L;
        User targetAdmin = new User(targetUserId, "target@test.ru", "Target", "password", UserRole.ROLE_ADMIN);

        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(List.of(targetAdmin));

        assertThatThrownBy(() -> userService.demoteFromAdmin(targetUserId))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    void whenGetAllAdminsAsAdmin_thenReturnAdmins() {
        UserManagerMockHelper.setAdminUser();
        List<User> expectedAdmins = Instancio.ofList(User.class)
                .set(field(User::getRole), UserRole.ROLE_ADMIN)
                .create();
        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(expectedAdmins);

        List<User> result = userService.getAllAdmins();

        assertThat(result).isEqualTo(expectedAdmins);
        verify(userRepository).findByRole(UserRole.ROLE_ADMIN);
    }

    @Test
    void whenGetAllAdminsAsRegularUser_thenThrowAccessDeniedException() {
        UserManagerMockHelper.setRegularUser(1L);

        assertThatThrownBy(() -> userService.getAllAdmins())
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findByRole(any());
    }
}
