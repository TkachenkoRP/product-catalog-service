package com.my.dto;

public record ProductResponseDto(Long id, String name, CategoryResponseDto category, BrandResponseDto brand,
                                 Double price, Integer stock) {
}
