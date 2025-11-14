package com.my.service;

import com.my.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAll();

    Category getById(Long id);

    Category save(Category category);

    Category update(Long id, Category category);

    boolean deleteById(Long id);

    boolean existsByName(String name);
}
