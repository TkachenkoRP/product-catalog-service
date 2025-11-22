package com.my.mapper;

import com.my.model.Category;
import com.my.service.CategoryService;
import com.my.service.impl.CategoryServiceImpl;

public class CategoryMap {
    private final CategoryService categoryService;

    public CategoryMap() {
        this.categoryService = new CategoryServiceImpl();
    }

    public Category fromId(Long id) {
        return id != null ? categoryService.getById(id) : null;
    }
}
