package com.my.model;

public record ProductFilter(
        Long categoryId,
        Long brandId,
        Double minPrice,
        Double maxPrice,
        Integer minStock) {
}
