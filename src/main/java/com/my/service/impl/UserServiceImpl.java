package com.my.service.impl;

import com.my.mapper.UserMapper;
import com.my.model.UserRole;
import com.my.repository.UserRepository;
import com.my.service.UserService;
import com.my.model.User;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User registration(String email, String password, String name) {
        if (!isEmailAvailable(email)) {
            return null;
        }
        return userRepository.save(new User(email, password, name, UserRole.ROLE_USER));
    }

    @Override
    public User login(String email, String password) {
        return getUserByLoginAndPassword(email, password);
    }

    @Override
    public List<User> getAll() {
        return userRepository.getAll();
    }

    @Override
    public User getById(Long id) {
        return userRepository.getById(id).orElse(null);
    }

    @Override
    public User update(Long id, User sourceUser) {
        User updatedUser = getById(id);
        if (updatedUser == null || (!updatedUser.getEmail().equals(sourceUser.getEmail()) && !isEmailAvailable(sourceUser.getEmail()))) {
            return null;
        }
        UserMapper.INSTANCE.updateUser(sourceUser, updatedUser);
        return userRepository.update(updatedUser);
    }

    @Override
    public boolean delete(Long id) {
        return userRepository.deleteById(id);
    }

    private User getUserByLoginAndPassword(String email, String password) {
        return userRepository.getByEmailAndPassword(email, password).orElse(null);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.isPresentByEmail(email);
    }
}
