package com.my.service.impl;

import com.my.exception.AccessDeniedException;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.exception.LastAdminException;
import com.my.mapper.UserMapper;
import com.my.model.UserRole;
import com.my.repository.UserRepository;
import com.my.security.UserManager;
import com.my.service.UserService;
import com.my.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
        if (!UserManager.isAdmin()) {
            throw new AccessDeniedException("Требуются права администратора");
        }
        return userRepository.getAll();
    }

    @Override
    public User getById(Long id) {
        if (!UserManager.isCurrentUser(id) && !UserManager.isAdmin()) {
            throw new AccessDeniedException("Нет прав для выполнения операции");
        }
        return userRepository.getById(id).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format("Пользователь с id {0} не найден", id)));
    }

    @Override
    public User update(Long id, User sourceUser) {
        if (!UserManager.isCurrentUser(id) && !UserManager.isAdmin()) {
            throw new AccessDeniedException("Нет прав для выполнения операции");
        }
        User updatedUser = getById(id);
        if (!updatedUser.getEmail().equals(sourceUser.getEmail()) && !isEmailAvailable(sourceUser.getEmail())) {
            throw new AlreadyExistException(MessageFormat.format("{0} уже используется", sourceUser.getEmail()));
        }
        userMapper.updateUser(sourceUser, updatedUser);
        return userRepository.update(updatedUser);
    }

    @Override
    public boolean delete(Long id) {
        if (!UserManager.isCurrentUser(id) && !UserManager.isAdmin()) {
            throw new AccessDeniedException("Нет прав для выполнения операции");
        }

        User user = getById(id);
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            List<User> admins = userRepository.findByRole(UserRole.ROLE_ADMIN);
            if (admins.size() <= 1 && admins.get(0).getId().equals(id)) {
                throw new LastAdminException();
            }
        }

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

    @Override
    public User promoteToAdmin(Long userId) {
        if (!UserManager.isAdmin()) {
            throw new AccessDeniedException("Требуются права администратора");
        }
        User user = getById(userId);
        user.setRole(UserRole.ROLE_ADMIN);
        return userRepository.update(user);
    }

    @Override
    public User demoteFromAdmin(Long userId) {
        if (!UserManager.isAdmin()) {
            throw new AccessDeniedException("Требуются права администратора");
        }
        if (UserManager.isCurrentUser(userId)) {
            throw new AccessDeniedException("Нельзя снять права администратора у самого себя");
        }
        List<User> admins = userRepository.findByRole(UserRole.ROLE_ADMIN);
        if (admins.size() <= 1 && admins.get(0).getId().equals(userId)) {
            throw new LastAdminException();
        }
        User user = getById(userId);
        user.setRole(UserRole.ROLE_USER);
        return userRepository.update(user);
    }

    @Override
    public List<User> getAllAdmins() {
        if (!UserManager.isAdmin()) {
            throw new AccessDeniedException("Требуются права администратора");
        }
        return userRepository.findByRole(UserRole.ROLE_ADMIN);
    }
}
