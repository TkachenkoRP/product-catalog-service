package com.my.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserRegisterRequestDto(
        @NotBlank(message = "Поле email должно быть заполнено")
        @Email(message = "Введите корректный email")
        String email,
        @NotBlank(message = "Поле username должно быть заполнено")
        String username,
        @NotBlank(message = "Поле password должно быть заполнено")
        @Length(min = 6, message = "Пароль должен содержать минимум {min} символов")
        String password) {
}
