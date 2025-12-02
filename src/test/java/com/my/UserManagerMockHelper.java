package com.my;

import com.my.model.User;
import com.my.model.UserRole;
import com.my.security.UserManager;
import org.instancio.Instancio;

import static org.instancio.Select.field;

public class UserManagerMockHelper {
    public static void setAdminUser() {
        User admin = Instancio.of(User.class)
                .set(field(User::getRole), UserRole.ROLE_ADMIN)
                .create();
        UserManager.setLoggedInUser(admin);
    }

    public static void setRegularUser(Long userId) {
        User user = Instancio.of(User.class)
                .set(field(User::getId), userId)
                .set(field(User::getRole), UserRole.ROLE_USER)
                .create();
        UserManager.setLoggedInUser(user);
    }

    public static void setCurrentUser(User user) {
        UserManager.setLoggedInUser(user);
    }

    public static void clearUser() {
        UserManager.setLoggedInUser(null);
    }
}
