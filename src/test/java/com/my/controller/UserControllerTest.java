package com.my.controller;

import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.exception.AccessDeniedException;
import com.my.exception.EntityNotFoundException;
import com.my.exception.LastAdminException;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.model.UserRole;
import com.my.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest extends AbstractControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        ExceptionHandlerController exceptionHandlerController = new ExceptionHandlerController();
        setUpMockMvc(userController, exceptionHandlerController);
    }

    @Test
    void whenGetAllUsers_thenReturnUserList() throws Exception {
        List<User> users = Arrays.asList(
                new User(1L, "admin@test.ru", "Admin User", "admin123", UserRole.ROLE_ADMIN),
                new User(2L, "user@test.ru", "Regular User", "user123", UserRole.ROLE_USER)
        );
        List<UserResponseDto> responseDtos = Arrays.asList(
                new UserResponseDto(1L, "admin@test.ru", "Admin User", "ROLE_ADMIN"),
                new UserResponseDto(2L, "user@test.ru", "Regular User", "ROLE_USER")
        );

        when(userService.getAll()).thenReturn(users);
        when(userMapper.toDto(users)).thenReturn(responseDtos);

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/user",
                HttpStatus.OK
        );

        List<UserResponseDto> result = extractListFromResponse(response, UserResponseDto.class);

        assertThat(result)
                .hasSize(2)
                .extracting(UserResponseDto::email)
                .containsExactly("admin@test.ru", "user@test.ru");

        verify(userService).getAll();
    }

    @Test
    void whenGetUserById_thenReturnUser() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "test@test.ru", "Test User", "password", UserRole.ROLE_USER);
        UserResponseDto responseDto = new UserResponseDto(userId, "test@test.ru", "Test User", "ROLE_USER");

        when(userService.getById(userId)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/user/" + userId,
                HttpStatus.OK
        );

        UserResponseDto result = extractDataFromResponse(response, UserResponseDto.class);

        assertThat(result)
                .extracting(
                        UserResponseDto::id,
                        UserResponseDto::email,
                        UserResponseDto::username,
                        UserResponseDto::role
                )
                .containsExactly(userId, "test@test.ru", "Test User", "ROLE_USER");

        verify(userService).getById(userId);
    }

    @Test
    void whenGetNonExistingUserById_thenReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        when(userService.getById(nonExistingId))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/user/" + nonExistingId,
                HttpStatus.NOT_FOUND
        );

        assertThat(getResponseMessage(response)).contains("Пользователь не найден");
    }

    @Test
    void whenUpdateUser_thenReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        UserRequestDto requestDto = new UserRequestDto("updated@test.ru", "Updated User", "newpassword");
        User userEntity = new User("updated@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        User updatedUser = new User(userId, "updated@test.ru", "Updated User", "newpassword", UserRole.ROLE_USER);
        UserResponseDto responseDto = new UserResponseDto(userId, "updated@test.ru", "Updated User", "ROLE_USER");

        when(userMapper.toEntity(requestDto)).thenReturn(userEntity);
        when(userService.update(eq(userId), eq(userEntity))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/user/" + userId,
                requestDto,
                HttpStatus.OK
        );

        UserResponseDto result = extractDataFromResponse(response, UserResponseDto.class);

        assertThat(result)
                .extracting(UserResponseDto::email, UserResponseDto::username)
                .containsExactly("updated@test.ru", "Updated User");

        verify(userService).update(userId, userEntity);
    }

    @Test
    void whenUpdateUserWithInvalidEmail_thenReturnBadRequest() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("invalid-email", "ValidUsername", "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/user/1",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        assertThat(extractListFromResponse(response, String.class)).contains("Введите корректный email");
    }

    @Test
    void whenUpdateUserWithShortUsername_thenReturnBadRequest() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("valid@email.com", "Ab", "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/user/1",
                requestDto,
                HttpStatus.BAD_REQUEST
        );
        assertThat(extractListFromResponse(response, String.class)).contains("Имя пользователя должно быть от 3 до 50 символов");
    }

    @Test
    void whenUpdateUserWithLongUsername_thenReturnBadRequest() throws Exception {
        String longUsername = "A".repeat(51);
        UserRequestDto requestDto = new UserRequestDto("valid@email.com", longUsername, "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/user/1",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        assertThat(extractListFromResponse(response, String.class)).contains("Имя пользователя должно быть от 3 до 50 символов");
    }

    @Test
    void whenUpdateUserWithShortPassword_thenReturnBadRequest() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("valid@email.com", "ValidUsername", "123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/user/1",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        assertThat(extractListFromResponse(response, String.class)).contains("Пароль должен содержать минимум 6 символов");
    }

    @Test
    void whenDeleteUser_thenReturnSuccess() throws Exception {
        Long userId = 1L;
        when(userService.delete(userId)).thenReturn(true);

        MockHttpServletResponse response = performRequest(
                HttpMethod.DELETE,
                "/api/user/" + userId,
                HttpStatus.OK
        );

        assertThat(getResponseMessage(response)).contains("Пользователь успешно удален");
        verify(userService).delete(userId);
    }

    @Test
    void whenPromoteUserToAdmin_thenReturnPromotedUser() throws Exception {
        Long userId = 2L;
        User promotedUser = new User(userId, "user@test.ru", "Regular User", "password", UserRole.ROLE_ADMIN);
        UserResponseDto responseDto = new UserResponseDto(userId, "user@test.ru", "Regular User", "ROLE_ADMIN");

        when(userService.promoteToAdmin(userId)).thenReturn(promotedUser);
        when(userMapper.toDto(promotedUser)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + userId + "/promote",
                HttpStatus.OK
        );

        UserResponseDto result = extractDataFromResponse(response, UserResponseDto.class);

        assertThat(result)
                .extracting(
                        UserResponseDto::id,
                        UserResponseDto::email,
                        UserResponseDto::username,
                        UserResponseDto::role
                )
                .containsExactly(userId, "user@test.ru", "Regular User", "ROLE_ADMIN");

        verify(userService).promoteToAdmin(userId);
    }

    @Test
    void whenPromoteNonExistingUserToAdmin_thenReturnNotFound() throws Exception {
        Long nonExistingUserId = 999L;
        when(userService.promoteToAdmin(nonExistingUserId))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + nonExistingUserId + "/promote",
                HttpStatus.NOT_FOUND
        );

        assertThat(getResponseMessage(response)).contains("Пользователь не найден");

        verify(userService).promoteToAdmin(nonExistingUserId);
    }

    @Test
    void whenPromoteUserToAdminWithoutAdminRights_thenReturnForbidden() throws Exception {
        Long userId = 2L;
        when(userService.promoteToAdmin(userId))
                .thenThrow(new AccessDeniedException("Требуются права администратора"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + userId + "/promote",
                HttpStatus.FORBIDDEN
        );

        assertThat(getResponseMessage(response)).contains("Требуются права администратора");

        verify(userService).promoteToAdmin(userId);
    }

    @Test
    void whenDemoteUserFromAdmin_thenReturnDemotedUser() throws Exception {
        Long userId = 1L;
        User demotedUser = new User(userId, "admin@test.ru", "Admin User", "password", UserRole.ROLE_USER);
        UserResponseDto responseDto = new UserResponseDto(userId, "admin@test.ru", "Admin User", "ROLE_USER");

        when(userService.demoteFromAdmin(userId)).thenReturn(demotedUser);
        when(userMapper.toDto(demotedUser)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + userId + "/demote",
                HttpStatus.OK
        );

        UserResponseDto result = extractDataFromResponse(response, UserResponseDto.class);

        assertThat(result)
                .extracting(
                        UserResponseDto::id,
                        UserResponseDto::email,
                        UserResponseDto::username,
                        UserResponseDto::role
                )
                .containsExactly(userId, "admin@test.ru", "Admin User", "ROLE_USER");

        verify(userService).demoteFromAdmin(userId);
    }

    @Test
    void whenDemoteNonExistingUserFromAdmin_thenReturnNotFound() throws Exception {
        Long nonExistingUserId = 999L;
        when(userService.demoteFromAdmin(nonExistingUserId))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + nonExistingUserId + "/demote",
                HttpStatus.NOT_FOUND
        );

        assertThat(getResponseMessage(response)).contains("Пользователь не найден");

        verify(userService).demoteFromAdmin(nonExistingUserId);
    }

    @Test
    void whenDemoteUserFromAdminWithoutAdminRights_thenReturnForbidden() throws Exception {
        Long userId = 1L;
        when(userService.demoteFromAdmin(userId))
                .thenThrow(new AccessDeniedException("Требуются права администратора"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + userId + "/demote",
                HttpStatus.FORBIDDEN
        );

        assertThat(getResponseMessage(response)).contains("Требуются права администратора");

        verify(userService).demoteFromAdmin(userId);
    }

    @Test
    void whenDemoteSelfFromAdmin_thenReturnForbidden() throws Exception {
        Long userId = 1L;
        when(userService.demoteFromAdmin(userId))
                .thenThrow(new AccessDeniedException("Нельзя снять права администратора у самого себя"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + userId + "/demote",
                HttpStatus.FORBIDDEN
        );

        assertThat(getResponseMessage(response)).contains("Нельзя снять права администратора у самого себя");

        verify(userService).demoteFromAdmin(userId);
    }

    @Test
    void whenDemoteLastAdmin_thenReturnConflict() throws Exception {
        Long userId = 1L;
        when(userService.demoteFromAdmin(userId))
                .thenThrow(new LastAdminException("Нельзя лишить прав последнего администратора"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/user/" + userId + "/demote",
                HttpStatus.CONFLICT
        );

        assertThat(getResponseMessage(response)).contains("Ошибка изменения прав: Нельзя лишить прав последнего администратора");

        verify(userService).demoteFromAdmin(userId);
    }
}
