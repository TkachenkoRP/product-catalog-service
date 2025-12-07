package com.my.service;

import com.my.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogValidationService {
    private final ProductRepository productRepository;

    /**
     * Проверяет, есть ли товары в указанной категории.
     *
     * @param categoryId идентификатор категории
     * @return true если в категории есть товары, false в противном случае
     */
    public boolean categoryHasProducts(Long categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    /**
     * Проверяет, есть ли товары указанного бренда.
     *
     * @param brandId идентификатор бренда
     * @return true если у бренда есть товары, false в противном случае
     */
    public boolean brandHasProducts(Long brandId) {
        return productRepository.existsByBrandId(brandId);
    }
}
