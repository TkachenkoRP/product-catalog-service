package com.my.service;

import com.my.model.Brand;

import java.util.List;

/**
 * Сервис для управления брендами товаров.
 */
public interface BrandService {
    /**
     * Получает список всех брендов.
     *
     * @return список всех брендов в системе
     */
    List<Brand> getAll();

    /**
     * Получает бренд по идентификатору.
     *
     * @param id идентификатор бренда
     * @return бренд с указанным идентификатором или {@code null} если не найден
     */
    Brand getById(Long id);

    /**
     * Сохраняет новый бренд в системе.
     *
     * @param brand бренд для сохранения
     * @return сохраненный бренд с присвоенным идентификатором
     */
    Brand save(Brand brand);

    /**
     * Обновляет существующий бренд.
     *
     * @param id    идентификатор обновляемого бренда
     * @param brand новые данные бренда
     * @return обновленный бренд или {@code null} если бренд не найден
     */
    Brand update(Long id, Brand brand);

    /**
     * Удаляет бренд по идентификатору.
     *
     * @param id идентификатор удаляемого бренда
     * @return {@code true} если удаление прошло успешно, {@code false} если бренд не найден
     */
    boolean deleteById(Long id);

    /**
     * Проверяет существование бренда с указанным названием.
     *
     * @param name название бренда для проверки
     * @return {@code true} если бренд с таким названием существует, {@code false} в противном случае
     */
    boolean existsByName(String name);
}
