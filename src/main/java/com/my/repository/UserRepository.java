package com.my.repository;

import com.my.model.User;
import com.my.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями в системе хранения данных.
 */
public interface UserRepository {
    /**
     * Получает список всех пользователей из хранилища.
     *
     * @return список всех пользователей
     */
    List<User> findAll();

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return Optional с пользователем, если найден, иначе пустой Optional
     */
    Optional<User> findById(Long id);

    /**
     * Сохраняет нового пользователя в хранилище.
     *
     * @param user пользователь для сохранения
     * @return сохраненный пользователь с присвоенным идентификатором
     */
    User save(User user);

    /**
     * Обновляет существующего пользователя в хранилище.
     *
     * @param user пользователь с обновленными данными
     * @return обновленный пользователь
     */
    User update(User user);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор удаляемого пользователя
     * @return true если удаление прошло успешно, false если пользователь не найден
     */
    boolean deleteById(Long id);

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email email для проверки
     * @return true если пользователь с таким email существует, иначе false
     */
    boolean isPresentByEmail(String email);

    /**
     * Находит пользователя по email и паролю для аутентификации.
     *
     * @param email    email пользователя
     * @param password пароль пользователя
     * @return Optional с пользователем, если найден с указанными учетными данными, иначе пустой Optional
     */
    Optional<User> getByEmailAndPassword(String email, String password);

    /**
     * Найти всех пользователей с указанной ролью
     * @param role роль пользователя
     * @return список пользователей
     */
    List<User> findByRole(UserRole role);
}
