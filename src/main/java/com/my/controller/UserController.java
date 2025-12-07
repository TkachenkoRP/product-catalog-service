package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.ApiResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAll() {
        List<User> userList = userService.getAll();
        List<UserResponseDto> dtos = userMapper.toDto(userList);
        return ResponseEntity.ok(ApiResponseDto.success(dtos));
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getById(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        UserResponseDto dto = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponseDto.success(dto));
    }

    @Operation(summary = "Обновить пользователя", description = "Обновляет существующего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> patch(@PathVariable("id") Long id,
                                                                 @RequestBody @Validated(ValidationGroups.Update.class) UserRequestDto request) {
        User entity = userMapper.toEntity(request);
        User updated = userService.update(id, entity);
        UserResponseDto dto = userMapper.toDto(updated);
        return ResponseEntity.ok(ApiResponseDto.success("Пользователь успешно обновлен", dto));
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Пользователь успешно удален"));
    }

    @Operation(summary = "Назначить администратором", description = "Назначает указанного пользователя администратором системы.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно назначен администратором"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав. Требуется роль администратора."),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден")
    })
    @PostMapping("/{id}/promote")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> promoteToAdmin(@PathVariable("id") Long id) {
        User user = userService.promoteToAdmin(id);
        UserResponseDto dto = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponseDto.success("User promoted to admin", dto));
    }

    @Operation(summary = "Лишить прав администратора", description = "Лишает указанного пользователя прав администратора системы.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно лишен прав администратора"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав. Требуется роль администратора."),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден")
    })
    @PostMapping("/{id}/demote")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> demoteFromAdmin(@PathVariable("id") Long id) {
        User user = userService.demoteFromAdmin(id);
        UserResponseDto dto = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponseDto.success("User demoted from admin", dto));
    }
}
