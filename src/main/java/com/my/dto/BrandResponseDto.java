package com.my.dto;

/**
 * DTO для передачи данных о бренде в ответах API.
 *
 * @param id   уникальный идентификатор бренда
 * @param name название бренда
 */
public record BrandResponseDto(Long id, String name) {
}
