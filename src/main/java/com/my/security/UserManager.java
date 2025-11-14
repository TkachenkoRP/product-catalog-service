package com.my.security;

import com.my.model.User;
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
}
