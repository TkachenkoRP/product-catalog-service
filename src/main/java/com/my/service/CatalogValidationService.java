package com.my.service;

import com.my.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogValidationService {
    private final ProductRepository productRepository;

    public boolean categoryHasProducts(Long categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    public boolean brandHasProducts(Long brandId) {
        return productRepository.existsByBrandId(brandId);
    }
}
