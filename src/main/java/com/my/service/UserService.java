package com.my.service;

import com.my.model.User;

import java.util.List;

public interface UserService {
    User registration(String email, String name, String password);

    User login(String email, String password);

    List<User> getAll();

    User getById(Long id);

    User update(Long id, User sourceUser);

    boolean delete(Long id);

    boolean isEmailAvailable(String email);
}
