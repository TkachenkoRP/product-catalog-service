package com.my.util;

/**
 * Утилитный класс для генерации ключей кэша.
 * Содержит статические методы для формирования строковых ключей,
 * используемых при работе с кэш-хранилищами.
 */
public class CacheKeyGenerator {
    /**
     * Приватный конструктор для предотвращения создания экземпляров утилитного класса.
     */
    private CacheKeyGenerator() {
    }

    /**
     * Генерирует ключ для конкретного продукта по его идентификатору.
     *
     * @param id идентификатор продукта
     * @return строковый ключ в формате "product:{id}"
     */
    public static String generateProductKey(Long id) {
        return "product:" + id;
    }

    /**
     * Генерирует ключ для получения всех продуктов.
     *
     * @return строковый ключ "products:all"
     */
    public static String generateAllProductsKey() {
        return "products:all";
    }

    /**
     * Генерирует ключ для конкретной категории по её идентификатору.
     *
     * @param id идентификатор категории
     * @return строковый ключ в формате "category:{id}"
     */
    public static String generateCategoryKey(Long id) {
        return "category:" + id;
    }

    /**
     * Генерирует ключ для получения всех категорий.
     *
     * @return строковый ключ "categories:all"
     */
    public static String generateAllCategoriesKey() {
        return "categories:all";
    }

    /**
     * Генерирует ключ для конкретного бренда по его идентификатору.
     *
     * @param id идентификатор бренда
     * @return строковый ключ в формате "brand:{id}"
     */
    public static String generateBrandKey(Long id) {
        return "brand:" + id;
    }

    /**
     * Генерирует ключ для получения всех брендов.
     *
     * @return строковый ключ "brands:all"
     */
    public static String generateAllBrandsKey() {
        return "brands:all";
    }
}
