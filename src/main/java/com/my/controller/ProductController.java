package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.ProductRequestDto;
import com.my.dto.ProductResponseDto;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.service.ProductService;
import com.my.validation.ValidationGroups;
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
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public List<ProductResponseDto> getAll(@ModelAttribute ProductFilter filter) {
        List<Product> categories = productService.getAll(filter);
        return productMapper.toDto(categories);
    }

    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable("id") Long id) {
        Product category = productService.getById(id);
        return productMapper.toDto(category);
    }

    @PostMapping
    public ProductResponseDto post(@RequestBody @Validated(ValidationGroups.Create.class) ProductRequestDto request) {
        Product entity = productMapper.toEntity(request);
        Product saved = productService.save(entity);
        return productMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public ProductResponseDto patch(@PathVariable("id") Long id,
                                    @RequestBody @Validated(ValidationGroups.Update.class) ProductRequestDto request) {
        Product entity = productMapper.toEntity(request);
        Product updated = productService.update(id, entity);
        return productMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        productService.deleteById(id);
    }
}
