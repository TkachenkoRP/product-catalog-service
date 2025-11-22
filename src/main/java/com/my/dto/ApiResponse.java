package com.my.dto;

import com.my.security.UserManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Long currentUserId;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, UserManager.getCurrentUserId());
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, UserManager.getCurrentUserId());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, UserManager.getCurrentUserId());
    }
}
