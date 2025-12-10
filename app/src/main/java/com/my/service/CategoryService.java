package com.my.service;

import com.my.model.Category;

import java.util.List;

/**
 * Сервис для управления категориями товаров.
 */
public interface CategoryService {
    /**
     * Получает список всех категорий.
     *
     * @return список всех категорий в системе
     */
    List<Category> getAll();

    /**
     * Получает категорию по идентификатору.
     *
     * @param id идентификатор категории
     * @return категория с указанным идентификатором или {@code null} если не найдена
     */
    Category getById(Long id);

    /**
     * Сохраняет новую категорию в системе.
     *
     * @param category категория для сохранения
     * @return сохраненная категория с присвоенным идентификатором
     */
    Category save(Category category);

    /**
     * Обновляет существующую категорию.
     *
     * @param id       идентификатор обновляемой категории
     * @param category новые данные категории
     * @return обновленная категория или {@code null} если категория не найдена
     */
    Category update(Long id, Category category);

    /**
     * Удаляет категорию по идентификатору.
     *
     * @param id идентификатор удаляемой категории
     * @return {@code true} если удаление прошло успешно, {@code false} если категория не найдена
     */
    boolean deleteById(Long id);

    /**
     * Проверяет существование категории с указанным названием.
     *
     * @param name название категории для проверки
     * @return {@code true} если категория с таким названием существует, {@code false} в противном случае
     */
    boolean existsByName(String name);
}
