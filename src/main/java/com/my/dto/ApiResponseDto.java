package com.my.dto;

/**
 * Универсальный формат ответа API.
 *
 * @param <T>     тип данных в ответе
 * @param success true если операция успешна, false если произошла ошибка
 * @param message сообщение для пользователя (успех или ошибка)
 * @param data    полезные данные ответа (null при ошибках)
 */
public record ApiResponseDto<T>(
        boolean success,
        String message,
        T data
) {
    /**
     * Создает успешный ответ с данными.
     *
     * @param <T>  тип данных
     * @param data данные для возврата
     * @return успешный ApiResponse
     */
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, "Успешно", data);
    }

    /**
     * Создает успешный ответ с кастомным сообщением и данными.
     *
     * @param <T>     тип данных
     * @param message кастомное сообщение
     * @param data    данные для возврата
     * @return успешный ApiResponse
     */
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data);
    }

    /**
     * Создает успешный ответ без данных (только сообщение).
     *
     * @param message сообщение об успехе
     * @return успешный ApiResponse без данных
     */
    public static ApiResponseDto<Void> success(String message) {
        return new ApiResponseDto<>(true, message, null);
    }

    /**
     * Создает ответ об ошибке.
     *
     * @param message сообщение об ошибке
     * @return ApiResponse с признаком ошибки
     */
    public static ApiResponseDto<Void> error(String message) {
        return new ApiResponseDto<>(false, message, null);
    }

    /**
     * Создает ответ об ошибке с данными (например, валидационные ошибки).
     *
     * @param <T>       тип дополнительных данных об ошибке
     * @param message   сообщение об ошибке
     * @param errorData дополнительные данные об ошибке
     * @return ApiResponse с признаком ошибки и данными
     */
    public static <T> ApiResponseDto<T> error(String message, T errorData) {
        return new ApiResponseDto<>(false, message, errorData);
    }
}
