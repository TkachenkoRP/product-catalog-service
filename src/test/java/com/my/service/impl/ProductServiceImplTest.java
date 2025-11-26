package com.my.service.impl;

import com.my.exception.EntityNotFoundException;
import com.my.model.Product;
import com.my.repository.ProductRepository;
import com.my.service.CacheService;
import com.my.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CacheService cacheService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, cacheService);
    }

    @Test
    void testGetAllWithoutFilter() {
        List<Product> expectedProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(2L, "Product 2", 2L, 2L, 149.99, 5)
        );

        when(productRepository.getAll(null)).thenReturn(expectedProducts);

        List<Product> result = productService.getAll(null);

        assertThat(result).isEqualTo(expectedProducts);
    }

    @Test
    void testGetByIdWithCache() {
        Product expectedProduct = new Product(1L, "Test Product", 1L, 1L, 99.99, 10);

        when(cacheService.get("PRODUCT1")).thenReturn(null);
        when(productRepository.getById(1L)).thenReturn(Optional.of(expectedProduct));

        Product result = productService.getById(1L);

        assertThat(result).isEqualTo(expectedProduct);
        verify(cacheService).put("PRODUCT1", expectedProduct);
    }

    @Test
    void testGetByIdFromCache() {
        Product cachedProduct = new Product(1L, "Cached Product", 1L, 1L, 99.99, 10);

        when(cacheService.get("PRODUCT1")).thenReturn(cachedProduct);

        Product result = productService.getById(1L);

        assertThat(result).isEqualTo(cachedProduct);
        verify(productRepository, never()).getById(any());
    }

    @Test
    void testGetByIdNotFound() {
        when(cacheService.get("PRODUCT1")).thenReturn(null);
        when(productRepository.getById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Товар с id 1 не найден");
    }

    @Test
    void testSave() {
        Product productToSave = new Product("New Product", 1L, 1L, 99.99, 10);
        Product savedProduct = new Product(1L, "New Product", 1L, 1L, 99.99, 10);

        when(productRepository.save(productToSave)).thenReturn(savedProduct);

        Product result = productService.save(productToSave);

        assertThat(result).isEqualTo(savedProduct);
        verify(cacheService).invalidate("ALL_PRODUCTS");
    }

    @Test
    void testUpdateNotFound() {
        Product sourceProduct = new Product("Updated Product", 2L, 2L, 149.99, 5);

        when(productRepository.getById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, sourceProduct))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Товар с id 1 не найден");
    }

    @Test
    void testDeleteByIdSuccess() {
        when(productRepository.deleteById(1L)).thenReturn(true);

        boolean result = productService.deleteById(1L);

        assertThat(result).isTrue();
        verify(cacheService).invalidate("PRODUCT1");
        verify(cacheService).invalidate("ALL_PRODUCTS");
    }

    @Test
    void testDeleteByIdFailure() {
        when(productRepository.deleteById(1L)).thenReturn(false);

        boolean result = productService.deleteById(1L);

        assertThat(result).isFalse();
        verify(cacheService, never()).invalidate(any());
    }

    @Test
    void testCacheInvalidationOnSave() {
        Product product = new Product("New Product", 1L, 1L, 99.99, 10);
        Product savedProduct = new Product(1L, "New Product", 1L, 1L, 99.99, 10);

        when(productRepository.save(product)).thenReturn(savedProduct);

        productService.save(product);

        verify(cacheService).invalidate("ALL_PRODUCTS");
        verify(cacheService, never()).invalidate("PRODUCT1");
    }

    @Test
    void testCacheInvalidationOnDelete() {
        when(productRepository.deleteById(1L)).thenReturn(true);

        productService.deleteById(1L);

        verify(cacheService).invalidate("PRODUCT1");
        verify(cacheService).invalidate("ALL_PRODUCTS");
    }
}
