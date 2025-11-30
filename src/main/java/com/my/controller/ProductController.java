package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.ProductRequestDto;
import com.my.dto.ProductResponseDto;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.service.ProductService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Audition
@Tag(name = "Продукты", description = "API для управления продуктами")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "Получить все продукты", description = "Возвращает список всех продуктов с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка продуктов")
    @GetMapping
    public List<ProductResponseDto> getAll(@ModelAttribute ProductFilter filter) {
        List<Product> categories = productService.getAll(filter);
        return productMapper.toDto(categories);
    }

    @Operation(summary = "Получить продукт по ID", description = "Возвращает продукт по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт найден"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable("id") Long id) {
        Product category = productService.getById(id);
        return productMapper.toDto(category);
    }

    @Operation(summary = "Создать новый продукт", description = "Создает новый продукт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content)
    })
    @PostMapping
    public ProductResponseDto post(@RequestBody @Validated(ValidationGroups.Create.class) ProductRequestDto request) {
        Product entity = productMapper.toEntity(request);
        Product saved = productService.save(entity);
        return productMapper.toDto(saved);
    }

    @Operation(summary = "Обновить продукт", description = "Обновляет существующий продукт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Продукт не найден", content = @Content)
    })
    @PatchMapping("/{id}")
    public ProductResponseDto patch(@PathVariable("id") Long id,
                                    @RequestBody @Validated(ValidationGroups.Update.class) ProductRequestDto request) {
        Product entity = productMapper.toEntity(request);
        Product updated = productService.update(id, entity);
        return productMapper.toDto(updated);
    }

    @Operation(summary = "Удалить продукт", description = "Удаляет продукт по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт успешно удален"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден", content = @Content),
            @ApiResponse(responseCode = "409", description = "Продукт имеет ссылки", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        productService.deleteById(id);
    }
}
