package com.my.dto;

/**
 * DTO для передачи данных о товаре в ответах API.
 *
 * @param id       уникальный идентификатор товара
 * @param name     название товара
 * @param category информация о категории товара
 * @param brand    информация о бренде товара
 * @param price    цена товара
 * @param stock    количество товара на складе
 */
public record ProductResponseDto(Long id, String name, CategoryResponseDto category, BrandResponseDto brand,
                                 Double price, Integer stock) {
}
