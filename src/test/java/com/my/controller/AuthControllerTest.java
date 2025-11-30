package com.my.controller;

import com.my.dto.ErrorResponseDto;
import com.my.dto.UserLoginRequestDto;
import com.my.dto.UserRegisterRequestDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest extends AbstractControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ExceptionHandlerController exceptionHandlerController = new ExceptionHandlerController();
        setUpMockMvc(authController, exceptionHandlerController);
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnUser() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("test@test.ru", "password123");
        User user = new User(1L, "test@test.ru", "Test User", "password123", UserRole.ROLE_USER);
        UserResponseDto responseDto = new UserResponseDto(1L, "test@test.ru", "Test User", "ROLE_USER");

        when(userService.login("test@test.ru", "password123")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/login",
                requestDto,
                HttpStatus.OK
        );

        UserResponseDto result = fromResponse(response, UserResponseDto.class);

        assertThat(result)
                .extracting(UserResponseDto::email, UserResponseDto::username)
                .containsExactly("test@test.ru", "Test User");

        verify(userService).login("test@test.ru", "password123");
    }

    @Test
    void whenLoginWithInvalidCredentials_thenReturnNotFound() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("wrong@test.ru", "wrongpassword");

        when(userService.login("wrong@test.ru", "wrongpassword"))
                .thenThrow(new EntityNotFoundException("Введены неверные данные"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/login",
                requestDto,
                HttpStatus.NOT_FOUND
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Введены неверные данные");
    }

    @Test
    void whenLoginWithBlankEmail_thenReturnBadRequest() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("", "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/login",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле email должно быть заполнено");
    }

    @Test
    void whenLoginWithInvalidEmail_thenReturnBadRequest() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("invalid-email", "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/login",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Введите корректный email");
    }

    @Test
    void whenLoginWithBlankPassword_thenReturnBadRequest() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("valid@email.com", "");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/login",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле password должно быть заполнено");
    }

    @Test
    void whenLogout_thenReturnSuccess() throws Exception {
        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/logout",
                HttpStatus.OK
        );

        assertThat(response.getContentAsString()).isEmpty();
        verify(userService).logout();
    }

    @Test
    void whenRegisterWithAvailableEmail_thenReturnUser() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("newuser@test.ru", "New User", "password123");
        User user = new User(1L, "newuser@test.ru", "New User", "password123", UserRole.ROLE_USER);
        UserResponseDto responseDto = new UserResponseDto(1L, "newuser@test.ru", "New User", "ROLE_USER");

        when(userService.registration("newuser@test.ru", "New User", "password123")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/register",
                requestDto,
                HttpStatus.OK
        );

        UserResponseDto result = fromResponse(response, UserResponseDto.class);

        assertThat(result)
                .extracting(UserResponseDto::email, UserResponseDto::username)
                .containsExactly("newuser@test.ru", "New User");

        verify(userService).registration("newuser@test.ru", "New User", "password123");
    }

    @Test
    void whenRegisterWithBlankEmail_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("", "ValidUsername", "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/register",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле email должно быть заполнено");
    }

    @Test
    void whenRegisterWithInvalidEmail_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("invalid-email", "ValidUsername", "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/register",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Введите корректный email");
    }

    @Test
    void whenRegisterWithBlankUsername_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("valid@email.com", "", "password123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/register",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле username должно быть заполнено");
    }

    @Test
    void whenRegisterWithShortPassword_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("valid@email.com", "ValidUsername", "123");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/auth/register",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Пароль должен содержать минимум 6 символов");
    }
}
