package com.my.dto;

public record ProductRequestDto(String name, Long categoryId, Long brandId,
                                Double price, Integer stock) {
}
