package com.my.service;

import java.util.List;

public interface CacheService {
    /**
     * Сохраняет объект в кэш с TTL по умолчанию.
     *
     * @param key   ключ для сохранения
     * @param value объект для кэширования
     */
    void put(String key, Object value);

    /**
     * Получает объект из кэша по ключу.
     *
     * @param <T>   тип возвращаемого объекта
     * @param key   ключ для поиска
     * @param clazz класс возвращаемого объекта
     * @return объект из кэша или null, если не найден
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Получает список объектов из кэша по ключу.
     *
     * @param <T>   тип элементов списка
     * @param key   ключ для поиска
     * @param clazz класс элементов списка
     * @return список объектов из кэша или null, если не найден
     */
    <T> List<T> getList(String key, Class<T> clazz);

    /**
     * Удаляет объект из кэша по ключу.
     *
     * @param key ключ для удаления
     */
    void invalidate(String key);
}
