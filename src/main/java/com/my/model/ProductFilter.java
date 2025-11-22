package com.my.model;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record ProductFilter(
        Long categoryId,
        Long brandId,
        Double minPrice,
        Double maxPrice,
        Integer minStock) {
}
