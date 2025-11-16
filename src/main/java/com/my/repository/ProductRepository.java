package com.my.repository;

import com.my.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с товарами в системе хранения данных.
 */
public interface ProductRepository {
    /**
     * Получает список всех товаров из хранилища.
     *
     * @return список всех товаров
     */
    List<Product> getAll();

    /**
     * Находит товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return Optional с товаром, если найден, иначе пустой Optional
     */
    Optional<Product> getById(Long id);

    /**
     * Сохраняет новый товар в хранилище.
     *
     * @param product товар для сохранения
     * @return сохраненный товар с присвоенным идентификатором
     */
    Product save(Product product);

    /**
     * Обновляет существующий товар в хранилище.
     *
     * @param product товар с обновленными данными
     * @return обновленный товар
     */
    Product update(Product product);

    /**
     * Удаляет товар по идентификатору.
     *
     * @param id идентификатор удаляемого товара
     * @return true если удаление прошло успешно, false если товар не найден
     */
    boolean deleteById(Long id);
}
