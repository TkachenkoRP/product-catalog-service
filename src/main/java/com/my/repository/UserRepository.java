package com.my.repository;

import com.my.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getAll();

    Optional<User> getById(Long id);

    User save(User user);

    User update(User user);

    boolean deleteById(Long id);

    boolean isPresentByEmail(String email);

    Optional<User> getByEmailAndPassword(String email, String password);
}
