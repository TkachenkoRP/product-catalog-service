package com.my.dto;

import com.my.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "Поле email должно быть заполнено", groups = ValidationGroups.Create.class)
        @Email(message = "Введите корректный email", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        String email,
        @NotBlank(message = "Поле name должно быть заполнено", groups = ValidationGroups.Create.class)
        @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        String username,
        @NotBlank(message = "Поле password должно быть заполнено", groups = ValidationGroups.Create.class)
        @Size(min = 6, message = "Пароль должен содержать минимум {min} символов",
                groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        String password) {
}
