package com.my.mapper;

import com.my.dto.ProductDto;
import com.my.model.Brand;
import com.my.model.Category;
import com.my.model.Product;
import com.my.service.BrandService;
import com.my.service.CategoryService;

public class ProductMappingService {
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductMappingService(CategoryService categoryService, BrandService brandService) {
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = ProductMapper.INSTANCE.toDto(product);
        dto.setCategory(getCategoryById(product.getCategoryId()));
        dto.setBrand(getBrandById(product.getBrandId()));
        return dto;
    }

    private Category getCategoryById(Long id) {
        return id != null ? categoryService.getById(id) : null;
    }

    private Brand getBrandById(Long id) {
        return id != null ? brandService.getById(id) : null;
    }
}
