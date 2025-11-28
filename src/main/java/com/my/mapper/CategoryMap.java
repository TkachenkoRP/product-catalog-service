package com.my.mapper;

import com.my.model.Category;
import com.my.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryMap {
    private final CategoryService categoryService;

    @Autowired
    public CategoryMap(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public Category fromId(Long id) {
        return id != null ? categoryService.getById(id) : null;
    }
}
