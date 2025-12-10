package com.my.controller;

import com.my.dto.ApiResponseDto;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Товары", description = "API для управления товарами")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "Получить все товары", description = "Возвращает список всех товаров с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка товаров")
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> getAll(@ParameterObject @ModelAttribute ProductFilter filter) {
        List<Product> products = productService.getAll(filter);
        List<ProductResponseDto> dtos = productMapper.toDto(products);
        return ResponseEntity.ok(ApiResponseDto.success(dtos));
    }

    @Operation(summary = "Получить товар по ID", description = "Возвращает товар по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар найден"),
            @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> getById(@PathVariable("id") Long id) {
        Product product = productService.getById(id);
        ProductResponseDto dto = productMapper.toDto(product);
        return ResponseEntity.ok(ApiResponseDto.success(dto));
    }

    @Operation(summary = "Создать новый товар", description = "Создает новый товар")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> post(@RequestBody @Validated(ValidationGroups.Create.class) ProductRequestDto request) {
        Product entity = productMapper.toEntity(request);
        Product saved = productService.save(entity);
        ProductResponseDto dto = productMapper.toDto(saved);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Товар успешно создан", dto));
    }

    @Operation(summary = "Обновить товар", description = "Обновляет существующий товар")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> patch(@PathVariable("id") Long id,
                                                                    @RequestBody @Validated(ValidationGroups.Update.class) ProductRequestDto request) {
        Product entity = productMapper.toEntity(request);
        Product updated = productService.update(id, entity);
        ProductResponseDto dto = productMapper.toDto(updated);
        return ResponseEntity.ok(ApiResponseDto.success("Товар успешно обновлен", dto));
    }

    @Operation(summary = "Удалить товар", description = "Удаляет товар по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно удален"),
            @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content),
            @ApiResponse(responseCode = "409", description = "Товар имеет ссылки", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable("id") Long id) {
        productService.deleteById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Товар успешно удален"));
    }
}
