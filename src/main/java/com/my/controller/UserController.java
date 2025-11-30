package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.service.UserService;
import com.my.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Audition
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка пользователей")
    @GetMapping
    public List<UserResponseDto> getAll() {
        List<User> userList = userService.getAll();
        return userMapper.toDto(userList);
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public UserResponseDto getById(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        return userMapper.toDto(user);
    }

    @Operation(summary = "Обновить пользователя", description = "Обновляет существующего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @PatchMapping("/{id}")
    public UserResponseDto patch(@PathVariable("id") Long id,
                                 @RequestBody @Validated(ValidationGroups.Update.class) UserRequestDto request) {
        User entity = userMapper.toEntity(request);
        User updated = userService.update(id, entity);
        return userMapper.toDto(updated);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        userService.delete(id);
    }
}
