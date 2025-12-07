package com.my.controller;

import com.example.auditloggingstarter.annotation.EnableLoggingAspect;
import com.my.dto.UserLoginRequestDto;
import com.my.dto.UserRegisterRequestDto;
import com.my.dto.UserResponseDto;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableLoggingAspect
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и аутентификации пользователей")
public class AuthController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя по email и паролю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content)
    })
    @PostMapping("/login")
    public UserResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        User user = userService.login(request.email(), request.password());
        System.out.println(user);
        return userMapper.toDto(user);
    }

    @Operation(summary = "Выход из системы", description = "Завершение сессии пользователя")
    @ApiResponse(responseCode = "200", description = "Успешный выход из системы")
    @PostMapping("/logout")
    public void logout() {
        userService.logout();
    }

    @Operation(summary = "Регистрация", description = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Пользователь уже существует", content = @Content)
    })
    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Valid UserRegisterRequestDto request) {
        User user = userService.registration(request.email(), request.username(), request.password());
        return userMapper.toDto(user);
    }
}
