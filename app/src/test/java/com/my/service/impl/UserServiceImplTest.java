package com.my.service.impl;

import com.my.InstancioTestEntityFactory;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Тесты сервиса пользователей")
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
    @DisplayName("registration() - Успешная регистрация с доступным email")
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
    @DisplayName("registration() - Попытка регистрации с существующим email")
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
    @DisplayName("login() - Успешный вход с валидными учетными данными")
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
    @DisplayName("login() - Попытка входа с неверными учетными данными")
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
    @DisplayName("logout() - Успешный выход из системы")
    void whenLogout_thenClearLoggedInUser() {
        User loggedInUser = InstancioTestEntityFactory.createUser();
        UserManager.setLoggedInUser(loggedInUser);

        userService.logout();

        assertThat(UserManager.getLoggedInUser()).isNull();
    }

    @Test
    @DisplayName("getAll() - Получение всех пользователей администратором")
    void whenGetAllAsAdmin_thenReturnAllUsers() {
        UserManagerMockHelper.setAdminUser();
        int countUsers = 55;
        List<User> expectedUsers = InstancioTestEntityFactory.createUserList(countUsers);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAll();

        assertThat(result)
                .hasSize(countUsers)
                .isEqualTo(expectedUsers);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAll() - Попытка получения всех пользователей обычным пользователем")
    void whenGetAllAsRegularUser_thenThrowAccessDeniedException() {
        UserManagerMockHelper.setRegularUser(1L);

        assertThatThrownBy(() -> userService.getAll())
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findAll();
    }

    @Test
    @DisplayName("getAll() - Попытка получения всех пользователей неавторизованным пользователем")
    void whenGetAllAsUnauthenticated_thenThrowAccessDeniedException() {
        UserManagerMockHelper.clearUser();

        assertThatThrownBy(() -> userService.getAll())
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findAll();
    }

    @Test
    @DisplayName("getById() - Получение пользователя администратором")
    void whenGetByIdAsAdmin_thenReturnUser() {
        UserManagerMockHelper.setCurrentUser(InstancioTestEntityFactory.createAdminUser(2L));
        Long userId = 1L;
        User expectedUser = InstancioTestEntityFactory.createUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getById(userId);

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getById() - Получение собственного профиля")
    void whenGetByIdAsCurrentUser_thenReturnUser() {
        Long userId = 1L;
        User currentUser = InstancioTestEntityFactory.createUser(userId);
        UserManagerMockHelper.setRegularUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

        User result = userService.getById(userId);

        assertThat(result).isEqualTo(currentUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("getById() - Попытка получения профиля другого пользователя")
    void whenGetByIdAsOtherUser_thenThrowAccessDeniedException() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        UserManagerMockHelper.setRegularUser(currentUserId);

        assertThatThrownBy(() -> userService.getById(targetUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getById() - Попытка получения несуществующего пользователя")
    void whenGetNonExistingUserById_thenThrowException() {
        UserManagerMockHelper.setAdminUser(2L);
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь с id " + userId + " не найден");
    }

    @Test
    @DisplayName("update() - Обновление пользователя администратором")
    void whenUpdateAsAdmin_thenReturnUpdatedUser() {
        UserManagerMockHelper.setAdminUser(3L);
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
    @DisplayName("update() - Обновление собственного профиля")
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
    @DisplayName("update() - Попытка обновления профиля другого пользователя")
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
    @DisplayName("update() - Обновление пользователя с тем же email")
    void whenUpdateUserWithSameEmail_thenReturnUpdatedUser() {
        UserManagerMockHelper.setAdminUser(2L);
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
    @DisplayName("update() - Попытка обновления на существующий email другого пользователя")
    void whenUpdateUserWithExistingEmail_thenThrowException() {
        UserManagerMockHelper.setAdminUser(2L);
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
    @DisplayName("delete() - Удаление пользователя администратором")
    void whenDeleteAsAdmin_thenReturnSuccess() {
        UserManagerMockHelper.setAdminUser(2L);
        Long userId = 1L;
        User userToDelete = InstancioTestEntityFactory.createRegularUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        when(userRepository.deleteById(userId)).thenReturn(true);

        boolean result = userService.delete(userId);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("delete() - Удаление собственного профиля")
    void whenDeleteAsCurrentUser_thenReturnSuccess() {
        Long userId = 1L;
        User currentUser = InstancioTestEntityFactory.createRegularUser(userId);
        UserManagerMockHelper.setCurrentUser(currentUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.deleteById(userId)).thenReturn(true);

        boolean result = userService.delete(userId);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("delete() - Попытка удаления другого пользователя")
    void whenDeleteAsOtherUser_thenThrowAccessDeniedException() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        UserManagerMockHelper.setRegularUser(currentUserId);

        assertThatThrownBy(() -> userService.delete(targetUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete() - Попытка удаления последнего администратора")
    void whenDeleteLastAdminAsAdmin_thenThrowLastAdminException() {
        Long adminId = 1L;
        User admin = InstancioTestEntityFactory.createAdminUser(adminId);
        UserManagerMockHelper.setCurrentUser(admin);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(List.of(admin));

        assertThatThrownBy(() -> userService.delete(adminId))
                .isInstanceOf(LastAdminException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("isEmailAvailable() - Проверка доступности email")
    void whenCheckEmailAvailable_thenReturnTrue() {
        String email = "available@test.ru";
        when(userRepository.isPresentByEmail(email)).thenReturn(false);

        boolean result = userService.isEmailAvailable(email);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isEmailAvailable() - Проверка недоступности email")
    void whenCheckEmailNotAvailable_thenReturnFalse() {
        String email = "taken@test.ru";
        when(userRepository.isPresentByEmail(email)).thenReturn(true);

        boolean result = userService.isEmailAvailable(email);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("promoteToAdmin() - Повышение пользователя до администратора")
    void whenPromoteToAdminAsAdmin_thenReturnPromotedUser() {
        UserManagerMockHelper.setAdminUser(1L);
        Long userId = 2L;
        User user = InstancioTestEntityFactory.createRegularUser();
        User promotedUser = InstancioTestEntityFactory.createAdminUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.update(user)).thenReturn(promotedUser);

        User result = userService.promoteToAdmin(userId);

        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("promoteToAdmin() - Попытка повышения без прав администратора")
    void whenPromoteToAdminAsRegularUser_thenThrowAccessDeniedException() {
        UserManagerMockHelper.setRegularUser(1L);
        Long userId = 2L;

        assertThatThrownBy(() -> userService.promoteToAdmin(userId))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("demoteFromAdmin() - Понижение администратора до пользователя")
    void whenDemoteFromAdminAsAdmin_thenReturnDemotedUser() {
        Long adminId = 1L;
        User admin = InstancioTestEntityFactory.createAdminUser(adminId);
        UserManagerMockHelper.setCurrentUser(admin);
        Long targetUserId = 2L;
        User targetAdmin = InstancioTestEntityFactory.createAdminUser(targetUserId);
        User demotedUser = InstancioTestEntityFactory.createRegularUser(targetUserId);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(List.of(admin, targetAdmin));
        when(userRepository.update(targetAdmin)).thenReturn(demotedUser);

        User result = userService.demoteFromAdmin(targetUserId);

        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_USER);
        verify(userRepository).update(targetAdmin);
    }

    @Test
    @DisplayName("demoteFromAdmin() - Попытка понижения самого себя")
    void whenDemoteSelfFromAdmin_thenThrowAccessDeniedException() {
        Long adminId = 1L;
        User admin = InstancioTestEntityFactory.createAdminUser(adminId);
        UserManagerMockHelper.setCurrentUser(admin);

        assertThatThrownBy(() -> userService.demoteFromAdmin(adminId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("demoteFromAdmin() - Попытка понижения последнего администратора")
    void whenDemoteLastAdmin_thenThrowLastAdminException() {
        Long adminId = 1L;
        User admin = InstancioTestEntityFactory.createAdminUser(adminId);
        UserManagerMockHelper.setCurrentUser(admin);
        Long targetUserId = 2L;
        User targetAdmin = InstancioTestEntityFactory.createAdminUser(targetUserId);

        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(List.of(targetAdmin));

        assertThatThrownBy(() -> userService.demoteFromAdmin(targetUserId))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    @DisplayName("getAllAdmins() - Получение всех администраторов")
    void whenGetAllAdminsAsAdmin_thenReturnAdmins() {
        UserManagerMockHelper.setAdminUser();
        List<User> expectedAdmins = InstancioTestEntityFactory.createUserList(5);
        when(userRepository.findByRole(UserRole.ROLE_ADMIN)).thenReturn(expectedAdmins);

        List<User> result = userService.getAllAdmins();

        assertThat(result).isEqualTo(expectedAdmins);
        verify(userRepository).findByRole(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("getAllAdmins() - Попытка получения всех администраторов обычным пользователем")
    void whenGetAllAdminsAsRegularUser_thenThrowAccessDeniedException() {
        UserManagerMockHelper.setRegularUser(1L);

        assertThatThrownBy(() -> userService.getAllAdmins())
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findByRole(any());
    }
}
