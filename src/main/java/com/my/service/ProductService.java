package com.my.service;

import com.my.model.Product;
import com.my.model.ProductFilter;

import java.util.List;

/**
 * Сервис для управления товарами.
 */
public interface ProductService {
    /**
     * Получает список товаров с возможностью фильтрации.
     *
     * @param filter параметры фильтрации товаров (может быть {@code null} для получения всех товаров)
     * @return список товаров, соответствующих критериям фильтрации
     */
    List<Product> getAll(ProductFilter filter);

    /**
     * Получает товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return товар с указанным идентификатором или {@code null} если не найден
     */
    Product getById(Long id);

    /**
     * Сохраняет новый товар в системе.
     *
     * @param product товар для сохранения
     * @return сохраненный товар с присвоенным идентификатором
     */
    Product save(Product product);

    /**
     * Обновляет существующий товар.
     *
     * @param id      идентификатор обновляемого товара
     * @param product новые данные товара
     * @return обновленный товар или {@code null} если товар не найден
     */
    Product update(Long id, Product product);

    /**
     * Удаляет товар по идентификатору.
     *
     * @param id идентификатор удаляемого товара
     * @return {@code true} если удаление прошло успешно, {@code false} если товар не найден
     */
    boolean deleteById(Long id);
}
