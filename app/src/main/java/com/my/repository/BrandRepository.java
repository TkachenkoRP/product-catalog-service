package com.my.repository;

import com.my.model.Brand;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с брендами в системе хранения данных.
 */
public interface BrandRepository {
    /**
     * Находит список всех брендов из хранилища.
     *
     * @return список всех брендов
     */
    List<Brand> findAll();

    /**
     * Находит бренд по идентификатору.
     *
     * @param id идентификатор бренда
     * @return Optional с брендом, если найден, иначе пустой Optional
     */
    Optional<Brand> findById(Long id);

    /**
     * Сохраняет новый бренд в хранилище.
     *
     * @param brand бренд для сохранения
     * @return сохраненный бренд с присвоенным идентификатором
     */
    Brand save(Brand brand);

    /**
     * Обновляет существующий бренд в хранилище.
     *
     * @param brand бренд с обновленными данными
     * @return обновленный бренд
     */
    Brand update(Brand brand);

    /**
     * Удаляет бренд по идентификатору.
     *
     * @param id идентификатор удаляемого бренда
     * @return true если удаление прошло успешно, false если бренд не найден
     */
    boolean deleteById(Long id);

    /**
     * Проверяет существование бренда с указанным названием (без учета регистра).
     *
     * @param brandName название бренда для проверки
     * @return true если бренд с таким названием существует, иначе false
     */
    boolean existsByNameIgnoreCase(String brandName);
}
