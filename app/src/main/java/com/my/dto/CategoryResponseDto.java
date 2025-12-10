package com.my.dto;

/**
 * DTO для передачи данных о категории в ответах API.
 *
 * @param id   уникальный идентификатор категории
 * @param name название категории
 */
public record CategoryResponseDto(Long id, String name) {
}
