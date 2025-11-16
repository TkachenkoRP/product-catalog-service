package com.my.service;

import com.my.model.User;

import java.util.List;

/**
 * Сервис для управления пользователями.
 */
public interface UserService {
    /**
     * Регистрирует нового пользователя в системе.
     *
     * <p>Проверяет доступность email и создает нового пользователя с ролью ROLE_USER.
     *
     * @param email    email пользователя (должен быть уникальным)
     * @param password пароль пользователя
     * @param name     имя пользователя
     * @return зарегистрированный пользователь или {@code null} если email уже занят
     */
    User registration(String email, String name, String password);

    /**
     * Выполняет аутентификацию пользователя.
     *
     * @param email    email пользователя
     * @param password пароль пользователя
     * @return аутентифицированный пользователь или {@code null} при неверных учетных данных
     */
    User login(String email, String password);

    /**
     * Получает список всех пользователей.
     *
     * @return список всех пользователей в системе
     */
    List<User> getAll();

    /**
     * Получает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь с указанным идентификатором или {@code null} если не найден
     */
    User getById(Long id);

    /**
     * Обновляет данные пользователя.
     *
     * @param id         идентификатор обновляемого пользователя
     * @param sourceUser новые данные пользователя
     * @return обновленный пользователь или {@code null} если пользователь не найден
     */
    User update(Long id, User sourceUser);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор удаляемого пользователя
     * @return {@code true} если удаление прошло успешно, {@code false} если пользователь не найден
     */
    boolean delete(Long id);

    /**
     * Проверяет доступность email для регистрации.
     *
     * @param email email для проверки
     * @return {@code true} если email доступен, {@code false} если уже занят
     */
    boolean isEmailAvailable(String email);
}
