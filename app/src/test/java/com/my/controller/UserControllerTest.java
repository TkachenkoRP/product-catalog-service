package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.InstancioTestEntityFactory;
import com.my.dto.ApiResponseDto;
import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.exception.AccessDeniedException;
import com.my.exception.EntityNotFoundException;
import com.my.exception.LastAdminException;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.model.UserRole;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("Тесты контроллера пользователей")
class UserControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    public UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("GET /api/user - Получение всех пользователей")
    void whenGetAllUsers_thenReturnUserList() throws Exception {
        int countUsers = 10;
        List<User> users = InstancioTestEntityFactory.createUserList(countUsers);
        List<UserResponseDto> responseDtos = InstancioTestEntityFactory.createUserResponseDtos(users);

        when(userService.getAll()).thenReturn(users);
        when(userMapper.toDto(users)).thenReturn(responseDtos);

        String response = mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<UserResponseDto>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .hasSize(countUsers);

        verify(userService).getAll();
    }

    @Test
    @DisplayName("GET /api/user/{id} - Получение пользователя по ID")
    void whenGetUserById_thenReturnUser() throws Exception {
        Long userId = 1L;
        User user = InstancioTestEntityFactory.createUser(userId);
        UserResponseDto responseDto = InstancioTestEntityFactory.createUserResponseDto(user);

        when(userService.getById(userId)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        String response = mockMvc.perform(get("/api/user/" + userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(
                        UserResponseDto::id,
                        UserResponseDto::email,
                        UserResponseDto::username,
                        UserResponseDto::role
                )
                .containsExactly(userId, user.getEmail(), user.getUsername(), user.getRole().name());

        verify(userService).getById(userId);
    }

    @Test
    @DisplayName("GET /api/user/{id} - Несуществующий пользователь возвращает 404")
    void whenGetNonExistingUserById_thenReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        when(userService.getById(nonExistingId))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));

        String response = mockMvc.perform(get("/api/user/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Пользователь не найден");
    }

    @Test
    @DisplayName("PATCH /api/user/{id} - Обновление пользователя")
    void whenUpdateUser_thenReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        User userEntity = InstancioTestEntityFactory.createRegularUser(userId);
        UserRequestDto requestDto = InstancioTestEntityFactory.createUserRequestDto(userEntity);
        User updatedUser = InstancioTestEntityFactory.createRegularUser(userId);
        UserResponseDto responseDto = InstancioTestEntityFactory.createUserResponseDto(updatedUser);

        when(userMapper.toEntity(requestDto)).thenReturn(userEntity);
        when(userService.update(userId, userEntity)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(responseDto);

        String response = mockMvc.perform(patch("/api/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(UserResponseDto::email, UserResponseDto::username)
                .containsExactly(responseDto.email(), responseDto.username());

        verify(userService).update(userId, userEntity);
    }

    @Test
    @DisplayName("PATCH /api/user/{id} - Валидация: невалидный email")
    void whenUpdateUserWithInvalidEmail_thenReturnBadRequest() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("invalid-email", "ValidUsername", "password123");

        String response = mockMvc.perform(patch("/api/user/1")
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
    @DisplayName("PATCH /api/user/{id} - Валидация: короткое имя пользователя")
    void whenUpdateUserWithShortUsername_thenReturnBadRequest() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("valid@email.com", "Ab", "password123");

        String response = mockMvc.perform(patch("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Имя пользователя должно быть от 3 до 50 символов");
    }

    @Test
    @DisplayName("PATCH /api/user/{id} - Валидация: длинное имя пользователя")
    void whenUpdateUserWithLongUsername_thenReturnBadRequest() throws Exception {
        String longUsername = "A".repeat(51);
        UserRequestDto requestDto = new UserRequestDto("valid@email.com", longUsername, "password123");

        String response = mockMvc.perform(patch("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Имя пользователя должно быть от 3 до 50 символов");
    }

    @Test
    @DisplayName("PATCH /api/user/{id} - Валидация: короткий пароль")
    void whenUpdateUserWithShortPassword_thenReturnBadRequest() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("valid@email.com", "ValidUsername", "123");

        String response = mockMvc.perform(patch("/api/user/1")
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

    @Test
    @DisplayName("DELETE /api/user/{id} - Удаление пользователя")
    void whenDeleteUser_thenReturnSuccess() throws Exception {
        Long userId = 1L;
        when(userService.delete(userId)).thenReturn(true);

        String response = mockMvc.perform(delete("/api/user/" + userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<Void>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Пользователь успешно удален");
        verify(userService).delete(userId);
    }

    @Test
    @DisplayName("POST /api/user/{id}/promote - Повышение пользователя до администратора")
    void whenPromoteUserToAdmin_thenReturnPromotedUser() throws Exception {
        Long userId = 2L;
        User promotedUser = new User(userId, "user@test.ru", "Regular User", "password", UserRole.ROLE_ADMIN);
        UserResponseDto responseDto = new UserResponseDto(userId, "user@test.ru", "Regular User", "ROLE_ADMIN");

        when(userService.promoteToAdmin(userId)).thenReturn(promotedUser);
        when(userMapper.toDto(promotedUser)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/api/user/" + userId + "/promote"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
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
    @DisplayName("POST /api/user/{id}/promote - Попытка повышения несуществующего пользователя")
    void whenPromoteNonExistingUserToAdmin_thenReturnNotFound() throws Exception {
        Long nonExistingUserId = 999L;
        when(userService.promoteToAdmin(nonExistingUserId))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));

        String response = mockMvc.perform(post("/api/user/" + nonExistingUserId + "/promote"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Пользователь не найден");

        verify(userService).promoteToAdmin(nonExistingUserId);
    }

    @Test
    @DisplayName("POST /api/user/{id}/promote - Попытка повышения без прав администратора")
    void whenPromoteUserToAdminWithoutAdminRights_thenReturnForbidden() throws Exception {
        Long userId = 2L;
        when(userService.promoteToAdmin(userId))
                .thenThrow(new AccessDeniedException("Требуются права администратора"));

        String response = mockMvc.perform(post("/api/user/" + userId + "/promote"))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Требуются права администратора");

        verify(userService).promoteToAdmin(userId);
    }

    @Test
    @DisplayName("POST /api/user/{id}/demote - Понижение администратора до пользователя")
    void whenDemoteUserFromAdmin_thenReturnDemotedUser() throws Exception {
        Long userId = 1L;
        User demotedUser = new User(userId, "admin@test.ru", "Admin User", "password", UserRole.ROLE_USER);
        UserResponseDto responseDto = new UserResponseDto(userId, "admin@test.ru", "Admin User", "ROLE_USER");

        when(userService.demoteFromAdmin(userId)).thenReturn(demotedUser);
        when(userMapper.toDto(demotedUser)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/api/user/" + userId + "/demote"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<UserResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
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
    @DisplayName("POST /api/user/{id}/demote - Попытка понижения несуществующего пользователя")
    void whenDemoteNonExistingUserFromAdmin_thenReturnNotFound() throws Exception {
        Long nonExistingUserId = 999L;
        when(userService.demoteFromAdmin(nonExistingUserId))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));

        String response = mockMvc.perform(post("/api/user/" + nonExistingUserId + "/demote"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Пользователь не найден");

        verify(userService).demoteFromAdmin(nonExistingUserId);
    }

    @Test
    @DisplayName("POST /api/user/{id}/demote - Попытка понижения без прав администратора")
    void whenDemoteUserFromAdminWithoutAdminRights_thenReturnForbidden() throws Exception {
        Long userId = 1L;
        when(userService.demoteFromAdmin(userId))
                .thenThrow(new AccessDeniedException("Требуются права администратора"));

        String response = mockMvc.perform(post("/api/user/" + userId + "/demote"))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Требуются права администратора");

        verify(userService).demoteFromAdmin(userId);
    }

    @Test
    @DisplayName("POST /api/user/{id}/demote - Попытка понижения самого себя")
    void whenDemoteSelfFromAdmin_thenReturnForbidden() throws Exception {
        Long userId = 1L;
        when(userService.demoteFromAdmin(userId))
                .thenThrow(new AccessDeniedException("Нельзя снять права администратора у самого себя"));

        String response = mockMvc.perform(post("/api/user/" + userId + "/demote"))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Нельзя снять права администратора у самого себя");

        verify(userService).demoteFromAdmin(userId);
    }

    @Test
    @DisplayName("POST /api/user/{id}/demote - Попытка понижения последнего администратора")
    void whenDemoteLastAdmin_thenReturnConflict() throws Exception {
        Long userId = 1L;
        when(userService.demoteFromAdmin(userId))
                .thenThrow(new LastAdminException("Нельзя лишить прав последнего администратора"));

        String response = mockMvc.perform(post("/api/user/" + userId + "/demote"))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Ошибка изменения прав: Нельзя лишить прав последнего администратора");

        verify(userService).demoteFromAdmin(userId);
    }
}
