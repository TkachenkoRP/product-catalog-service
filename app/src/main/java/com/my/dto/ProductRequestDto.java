package com.my.dto;

import com.my.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о товаре в запросах API.
 *
 * @param name       название товара
 * @param categoryId идентификатор категории товара
 * @param brandId    идентификатор бренда товара
 * @param price      цена товара
 * @param stock      количество товара на складе
 */
public record ProductRequestDto(
        @NotBlank(message = "Поле name должно быть заполнено",
                groups = ValidationGroups.Create.class)
        @Size(min = 2, max = 200, message = "Название товара должно быть от 2 до 200 символов",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        String name,
        @NotNull(message = "Поле categoryId должно быть заполнено", groups = ValidationGroups.Create.class)
        @Positive(message = "ID категории должен быть положительным числом",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        Long categoryId,
        @NotNull(message = "Поле brandId должно быть заполнено", groups = ValidationGroups.Create.class)
        @Positive(message = "ID бренда должен быть положительным числом",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        Long brandId,
        @NotNull(message = "Поле price должно быть заполнено", groups = ValidationGroups.Create.class)
        @Positive(message = "Цена должна быть положительным числом",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        Double price,
        @NotNull(message = "Поле stock должно быть заполнено", groups = ValidationGroups.Create.class)
        @Positive(message = "Количество на складе должно быть положительным числом",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        Integer stock) {
}
