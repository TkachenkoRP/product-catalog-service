package com.my;

import com.my.model.User;
import com.my.security.UserManager;

/**
 * Вспомогательный класс для мокирования пользователя в UserManager.
 * Используется для установки различных ролей пользователя в тестах.
 */
public class UserManagerMockHelper {

    /**
     * Устанавливает пользователя с ролью администратора.
     */
    public static void setAdminUser() {
        User admin = InstancioTestEntityFactory.createAdminUser();
        UserManager.setLoggedInUser(admin);
    }

    /**
     * Устанавливает пользователя с ролью администратора и указанным ID.
     */
    public static void setAdminUser(Long id) {
        User admin = InstancioTestEntityFactory.createAdminUser(id);
        UserManager.setLoggedInUser(admin);
    }

    /**
     * Устанавливает обычного пользователя с указанным ID.
     */
    public static void setRegularUser(Long userId) {
        User user = InstancioTestEntityFactory.createRegularUser(userId);
        UserManager.setLoggedInUser(user);
    }

    /**
     * Устанавливает текущего пользователя.
     */
    public static void setCurrentUser(User user) {
        UserManager.setLoggedInUser(user);
    }

    /**
     * Очищает текущего пользователя.
     */
    public static void clearUser() {
        UserManager.setLoggedInUser(null);
    }
}
