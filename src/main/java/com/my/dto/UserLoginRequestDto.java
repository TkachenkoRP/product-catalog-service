package com.my.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @NotBlank(message = "Поле email должно быть заполнено")
        @Email(message = "Введите корректный email")
        String email,
        @NotBlank(message = "Поле password должно быть заполнено")
        String password) {
}
