package com.my.repository;

import com.my.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> getAll();

    Optional<Category> getById(Long id);

    Category save(Category category);

    Category update(Category category);

    boolean deleteById(Long id);

    boolean existsByNameIgnoreCase(String categoryName);
}
