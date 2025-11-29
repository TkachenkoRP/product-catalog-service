package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.service.UserService;
import lombok.RequiredArgsConstructor;
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
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserResponseDto> getAll() {
        List<User> userList = userService.getAll();
        return userMapper.toDto(userList);
    }

    @GetMapping("/{id}")
    public UserResponseDto getById(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        return userMapper.toDto(user);
    }

    @PatchMapping("/{id}")
    public UserResponseDto patch(@PathVariable("id") Long id,
                                 @RequestBody UserRequestDto request) {
        User entity = userMapper.toEntity(request);
        User updated = userService.update(id, entity);
        return userMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        userService.delete(id);
    }
}
