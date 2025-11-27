package com.my.controller;

import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public UserResponseDto login(@RequestBody UserRequestDto request) {
        User user = userService.login(request.email(), request.password());
        return userMapper.toDto(user);
    }

    @PostMapping("/logout")
    public void logout() {
        userService.logout();
    }

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody UserRequestDto request) {
        User user = userService.registration(request.email(), request.username(), request.password());
        return userMapper.toDto(user);
    }
}
