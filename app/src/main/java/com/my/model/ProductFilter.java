package com.my.model;

import lombok.experimental.FieldNameConstants;

/**
 * Критерии фильтрации товаров.
 * Используется для поиска товаров по различным параметрам.
 */
@FieldNameConstants
public record ProductFilter(
        Long categoryId,
        Long brandId,
        Double minPrice,
        Double maxPrice,
        Integer minStock) {
}
