package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.my.dto.ErrorResponseDto;
import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.exception.EntityNotFoundException;
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

        List<UserResponseDto> result = fromResponse(response, new TypeReference<>() {
        });

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

        UserResponseDto result = fromResponse(response, UserResponseDto.class);

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

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Пользователь не найден");
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

        UserResponseDto result = fromResponse(response, UserResponseDto.class);

        assertThat(result)
                .extracting(UserResponseDto::email, UserResponseDto::username)
                .containsExactly("updated@test.ru", "Updated User");

        verify(userService).update(userId, userEntity);
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

        assertThat(response.getContentAsString()).isEmpty();
        verify(userService).delete(userId);
    }
}
