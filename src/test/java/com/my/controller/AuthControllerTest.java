package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.InstancioTestEntityFactory;
import com.my.dto.ApiResponseDto;
import com.my.dto.UserLoginRequestDto;
import com.my.dto.UserRegisterRequestDto;
import com.my.dto.UserResponseDto;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@DisplayName("Тесты контроллера аутентификации")
class AuthControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    public AuthControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST /api/auth/login - Успешный вход с валидными учетными данными")
    void whenLoginWithValidCredentials_thenReturnUser() throws Exception {
        User user = InstancioTestEntityFactory.createRegularUser(1L);
        UserLoginRequestDto requestDto = InstancioTestEntityFactory.createUserLoginRequestDto(user);
        UserResponseDto responseDto = InstancioTestEntityFactory.createUserResponseDto(user);

        when(userService.login(any(), any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.success()).isTrue();
        assertThat(actualResponse.data()).isNotNull();

        verify(userService).login(any(), any());
    }

    @Test
    @DisplayName("POST /api/auth/login - Попытка входа с неверными учетными данными")
    void whenLoginWithInvalidCredentials_thenReturnNotFound() throws Exception {
        UserLoginRequestDto requestDto = InstancioTestEntityFactory.createUserLoginRequestDto();

        when(userService.login(requestDto.email(), requestDto.password()))
                .thenThrow(new EntityNotFoundException("Введены неверные данные"));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Введены неверные данные");
    }

    @Test
    @DisplayName("POST /api/auth/login - Валидация: пустой email")
    void whenLoginWithBlankEmail_thenReturnBadRequest() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("", "password123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле email должно быть заполнено");
    }

    @Test
    @DisplayName("POST /api/auth/login - Валидация: невалидный email")
    void whenLoginWithInvalidEmail_thenReturnBadRequest() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("invalid-email", "password123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Введите корректный email");
    }

    @Test
    @DisplayName("POST /api/auth/login - Валидация: пустой пароль")
    void whenLoginWithBlankPassword_thenReturnBadRequest() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("valid@email.com", "");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле password должно быть заполнено");
    }

    @Test
    @DisplayName("POST /api/auth/logout - Успешный выход из системы")
    void whenLogout_thenReturnSuccess() throws Exception {
        String response = mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Успешный выход из системы");
        verify(userService).logout();
    }

    @Test
    @DisplayName("POST /api/auth/register - Успешная регистрация пользователя")
    void whenRegisterWithAvailableEmail_thenReturnUser() throws Exception {
        User user = InstancioTestEntityFactory.createRegularUser(1L);
        UserRegisterRequestDto requestDto = InstancioTestEntityFactory.createUserRegisterRequestDto(user);
        UserResponseDto responseDto = InstancioTestEntityFactory.createUserResponseDto(user);

        when(userService.registration(requestDto.email(), requestDto.username(), requestDto.password())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(UserResponseDto::email, UserResponseDto::username)
                .containsExactly(requestDto.email(), requestDto.username());

        verify(userService).registration(requestDto.email(), requestDto.username(), requestDto.password());
    }

    @Test
    @DisplayName("POST /api/auth/register - Валидация: пустой email при регистрации")
    void whenRegisterWithBlankEmail_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("", "ValidUsername", "password123");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле email должно быть заполнено");
    }

    @Test
    @DisplayName("POST /api/auth/register - Валидация: невалидный email при регистрации")
    void whenRegisterWithInvalidEmail_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("invalid-email", "ValidUsername", "password123");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Введите корректный email");
    }

    @Test
    @DisplayName("POST /api/auth/register - Валидация: пустое имя пользователя при регистрации")
    void whenRegisterWithBlankUsername_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("valid@email.com", "", "password123");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле username должно быть заполнено");
    }

    @Test
    @DisplayName("POST /api/auth/register - Валидация: короткий пароль при регистрации")
    void whenRegisterWithShortPassword_thenReturnBadRequest() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("valid@email.com", "ValidUsername", "123");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Пароль должен содержать минимум 6 символов");
    }
}
