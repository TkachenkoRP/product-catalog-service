package com.my.security;

import com.my.model.User;
import com.my.model.UserRole;
import lombok.Getter;
import lombok.Setter;

/**
 * Утилитный класс для управления состоянием аутентифицированного пользователя.
 */
public class UserManager {
    @Setter
    @Getter
    private static User loggedInUser;

    private UserManager() {
    }

    /**
     * Проверяет, аутентифицирован ли пользователь.
     *
     * @return true если пользователь аутентифицирован, false в противном случае
     */
    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    /**
     * Завершает сессию текущего пользователя.
     */
    public static void logout() {
        loggedInUser = null;
    }

    /**
     * Проверяет, является ли текущий пользователь администратором.
     *
     * @return true если пользователь аутентифицирован и имеет роль ADMIN
     */
    public static boolean isAdmin() {
        return isLoggedIn() && loggedInUser.getRole() == UserRole.ROLE_ADMIN;
    }

    /**
     * Проверяет, является ли текущий пользователь обычным пользователем.
     *
     * @return true если пользователь аутентифицирован и имеет роль USER
     */
    public static boolean isUser() {
        return isLoggedIn() && loggedInUser.getRole() == UserRole.ROLE_USER;
    }

    /**
     * Проверяет, является ли указанный пользователь текущим.
     *
     * @param userId идентификатор пользователя для проверки
     * @return true если пользователь аутентифицирован и его ID совпадает с указанным
     */
    public static boolean isCurrentUser(Long userId) {
        return isLoggedIn() && loggedInUser.getId().equals(userId);
    }
}
