package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.CategoryRequestDto;
import com.my.dto.CategoryResponseDto;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.service.CategoryService;
import com.my.validation.ValidationGroups;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Audition
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryResponseDto> getAll() {
        List<Category> categories = categoryService.getAll();
        return categoryMapper.toDto(categories);
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable("id") Long id) {
        Category category = categoryService.getById(id);
        return categoryMapper.toDto(category);
    }

    @PostMapping
    public CategoryResponseDto post(@RequestBody @Validated(ValidationGroups.Create.class) CategoryRequestDto request) {
        Category entity = categoryMapper.toEntity(request);
        Category saved = categoryService.save(entity);
        return categoryMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public CategoryResponseDto patch(@PathVariable("id") Long id,
                                     @RequestBody @Validated(ValidationGroups.Update.class) CategoryRequestDto request) {
        Category entity = categoryMapper.toEntity(request);
        Category updated = categoryService.update(id, entity);
        return categoryMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        categoryService.deleteById(id);
    }
}
