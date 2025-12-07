package com.my.repository;

import com.my.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с категориями в системе хранения данных.
 */
public interface CategoryRepository {
    /**
     * Находит список всех категорий из хранилища.
     *
     * @return список всех категорий
     */
    List<Category> findAll();

    /**
     * Находит категорию по идентификатору.
     *
     * @param id идентификатор категории
     * @return Optional с категорией, если найдена, иначе пустой Optional
     */
    Optional<Category> findById(Long id);

    /**
     * Сохраняет новую категорию в хранилище.
     *
     * @param category категория для сохранения
     * @return сохраненная категория с присвоенным идентификатором
     */
    Category save(Category category);

    /**
     * Обновляет существующую категорию в хранилище.
     *
     * @param category категория с обновленными данными
     * @return обновленная категория
     */
    Category update(Category category);

    /**
     * Удаляет категорию по идентификатору.
     *
     * @param id идентификатор удаляемой категории
     * @return true если удаление прошло успешно, false если категория не найдена
     */
    boolean deleteById(Long id);

    /**
     * Проверяет существование категории с указанным названием (без учета регистра).
     *
     * @param categoryName название категории для проверки
     * @return true если категория с таким названием существует, иначе false
     */
    boolean existsByNameIgnoreCase(String categoryName);
}
