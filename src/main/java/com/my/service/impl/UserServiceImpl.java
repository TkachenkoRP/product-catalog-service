package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.UserMapper;
import com.my.model.UserRole;
import com.my.repository.UserRepository;
import com.my.repository.impl.PostgresqlUserRepositoryImpl;
import com.my.security.UserManager;
import com.my.service.UserService;
import com.my.model.User;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.util.List;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl() {
        this(new PostgresqlUserRepositoryImpl());
    }

    @Override
    public User registration(String email, String password, String name) {
        if (!isEmailAvailable(email)) {
            throw new AlreadyExistException(MessageFormat.format("{0} уже используется", email));
        }
        User saved = userRepository.save(new User(email, password, name, UserRole.ROLE_USER));
        UserManager.setLoggedInUser(saved);
        return saved;
    }

    @Override
    public User login(String email, String password) {
        User user = getUserByLoginAndPassword(email, password);
        UserManager.setLoggedInUser(user);
        return user;
    }

    @Override
    public void logout() {
        UserManager.logout();
    }

    @Override
    public List<User> getAll() {
        return userRepository.getAll();
    }

    @Override
    public User getById(Long id) {
        return userRepository.getById(id).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format("Пользователь с id {0} не найден", id)));
    }

    @Override
    public User update(Long id, User sourceUser) {
        User updatedUser = getById(id);
        if (!updatedUser.getEmail().equals(sourceUser.getEmail()) && !isEmailAvailable(sourceUser.getEmail())) {
            throw new AlreadyExistException(MessageFormat.format("{0} уже используется", sourceUser.getEmail()));
        }
        UserMapper.INSTANCE.updateUser(sourceUser, updatedUser);
        return userRepository.update(updatedUser);
    }

    @Override
    public boolean delete(Long id) {
        return userRepository.deleteById(id);
    }

    private User getUserByLoginAndPassword(String email, String password) {
        return userRepository.getByEmailAndPassword(email, password).orElseThrow(
                () -> new EntityNotFoundException("Введены неверные данные")
        );
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.isPresentByEmail(email);
    }
}
