package com.my.security;

import com.my.model.User;
import com.my.model.UserRole;
import lombok.Getter;
import lombok.Setter;

public class UserManager {
    @Setter
    @Getter
    private static User loggedInUser;

    private UserManager() {
    }

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static void logout() {
        loggedInUser = null;
    }

    public static Long getCurrentUserId() {
        return isLoggedIn() ? UserManager.getLoggedInUser().getId() : 0L;
    }

    public static boolean isAdmin() {
        return isLoggedIn() && loggedInUser.getRole() == UserRole.ROLE_ADMIN;
    }

    public static boolean isUser() {
        return isLoggedIn() && loggedInUser.getRole() == UserRole.ROLE_USER;
    }

    public static boolean isCurrentUser(Long userId) {
        return isLoggedIn() && loggedInUser.getId().equals(userId);
    }
}
