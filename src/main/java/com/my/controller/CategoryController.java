package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.CategoryRequestDto;
import com.my.dto.CategoryResponseDto;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.service.CategoryService;
import com.my.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Категории", description = "API для управления категориями")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "Получить все категории", description = "Возвращает список всех категорий")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка категорий")
    @GetMapping
    public List<CategoryResponseDto> getAll() {
        List<Category> categories = categoryService.getAll();
        return categoryMapper.toDto(categories);
    }

    @Operation(summary = "Получить категорию по ID", description = "Возвращает категорию по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория найдена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content)
    })
    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable("id") Long id) {
        Category category = categoryService.getById(id);
        return categoryMapper.toDto(category);
    }

    @Operation(summary = "Создать новую категорию", description = "Создает новую категорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content)
    })
    @PostMapping
    public CategoryResponseDto post(@RequestBody @Validated(ValidationGroups.Create.class) CategoryRequestDto request) {
        Category entity = categoryMapper.toEntity(request);
        Category saved = categoryService.save(entity);
        return categoryMapper.toDto(saved);
    }

    @Operation(summary = "Обновить категорию", description = "Обновляет существующую категорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content)
    })
    @PatchMapping("/{id}")
    public CategoryResponseDto patch(@PathVariable("id") Long id,
                                     @RequestBody @Validated(ValidationGroups.Update.class) CategoryRequestDto request) {
        Category entity = categoryMapper.toEntity(request);
        Category updated = categoryService.update(id, entity);
        return categoryMapper.toDto(updated);
    }

    @Operation(summary = "Удалить категорию", description = "Удаляет категорию по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content),
            @ApiResponse(responseCode = "409", description = "Категория имеет ссылки", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        categoryService.deleteById(id);
    }
}
