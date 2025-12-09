package com.my;

import com.my.model.User;
import com.my.security.UserManager;

public class UserManagerMockHelper {
    public static void setAdminUser() {
        User admin = InstancioTestEntityFactory.createAdminUser();
        UserManager.setLoggedInUser(admin);
    }

    public static void setRegularUser(Long userId) {
        User user = InstancioTestEntityFactory.createRegularUser(userId);
        UserManager.setLoggedInUser(user);
    }

    public static void setCurrentUser(User user) {
        UserManager.setLoggedInUser(user);
    }

    public static void clearUser() {
        UserManager.setLoggedInUser(null);
    }
}
