package com.my.dto;

/**
 * DTO для передачи данных о пользователе в ответах API.
 *
 * @param id       уникальный идентификатор пользователя
 * @param email    электронная почта пользователя
 * @param username имя пользователя
 * @param role     роль пользователя
 */
public record UserResponseDto(Long id, String email, String username, String role) {
}
