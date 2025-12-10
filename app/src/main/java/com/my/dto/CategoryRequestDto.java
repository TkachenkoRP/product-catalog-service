package com.my.dto;

import com.my.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о категории в запросах API.
 *
 * @param name название категории
 */
public record CategoryRequestDto(
        @NotBlank(message = "Поле name должно быть заполнено",
                groups = ValidationGroups.Create.class)
        @Size(min = 2, max = 100, message = "Название категории должно быть от 2 до 100 символов",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        String name) {
}
