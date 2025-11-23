package com.my.service.impl;

import com.my.model.AuditLog;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.ProductRepository;
import com.my.service.AuditService;
import com.my.service.CacheService;
import com.my.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private CacheService cacheService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, auditService, cacheService);
    }

    @Test
    void testGetAllWithoutFilter() {
        List<Product> expectedProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(2L, "Product 2", 2L, 2L, 149.99, 5)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(null);
        when(productRepository.getAll(null)).thenReturn(expectedProducts);

        List<Product> result = productService.getAll(null);

        assertThat(result).isEqualTo(expectedProducts);
        verify(cacheService).put("ALL_PRODUCTS", expectedProducts);
        verify(auditService).logAction(AuditLog.AuditActions.VIEW_ALL_PRODUCTS, "Просмотр всех товаров");
    }

    @Test
    void testGetAllWithFilterCategory() {
        List<Product> allProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(2L, "Product 2", 2L, 2L, 149.99, 5),
                new Product(3L, "Product 3", 1L, 3L, 79.99, 15)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(allProducts);

        ProductFilter filter = new ProductFilter(1L, null, null, null, null);
        List<Product> result = productService.getAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getId).containsExactly(1L, 3L);
        assertThat(result).extracting(Product::getCategoryId).containsOnly(1L);

        verify(auditService).logAction(eq(AuditLog.AuditActions.VIEW_ALL_PRODUCTS), anyString());
    }

    @Test
    void testGetAllWithFilterBrand() {
        List<Product> allProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(2L, "Product 2", 2L, 2L, 149.99, 5),
                new Product(3L, "Product 3", 1L, 1L, 79.99, 15)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(allProducts);

        ProductFilter filter = new ProductFilter(null, 1L, null, null, null);
        List<Product> result = productService.getAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getId).containsExactly(1L, 3L);
        assertThat(result).extracting(Product::getBrandId).containsOnly(1L);
    }

    @Test
    void testGetAllWithFilterPriceRange() {
        List<Product> allProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 50.0, 10),
                new Product(2L, "Product 2", 2L, 2L, 100.0, 5),
                new Product(3L, "Product 3", 1L, 1L, 150.0, 15)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(allProducts);

        ProductFilter filter = new ProductFilter(null, null, 75.0, 125.0, null);
        List<Product> result = productService.getAll(filter);

        assertThat(result).hasSize(1);
        assertThat(result).extracting(Product::getId).containsExactly(2L);
        assertThat(result).extracting(Product::getPrice).containsOnly(100.0);
    }

    @Test
    void testGetAllWithFilterMinPrice() {
        List<Product> allProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 50.0, 10),
                new Product(2L, "Product 2", 2L, 2L, 100.0, 5),
                new Product(3L, "Product 3", 1L, 1L, 150.0, 15)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(allProducts);

        ProductFilter filter = new ProductFilter(null, null, 100.0, null, null);
        List<Product> result = productService.getAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getId).containsExactly(2L, 3L);
        assertThat(result).extracting(Product::getPrice).allSatisfy(price ->
                assertThat(price).isGreaterThanOrEqualTo(100.0)
        );
    }

    @Test
    void testGetAllWithFilterMaxPrice() {
        List<Product> allProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 50.0, 10),
                new Product(2L, "Product 2", 2L, 2L, 100.0, 5),
                new Product(3L, "Product 3", 1L, 1L, 150.0, 15)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(allProducts);

        ProductFilter filter = new ProductFilter(null, null, null, 100.0, null);
        List<Product> result = productService.getAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getId).containsExactly(1L, 2L);
        assertThat(result).extracting(Product::getPrice).allSatisfy(price ->
                assertThat(price).isLessThanOrEqualTo(100.0)
        );
    }

    @Test
    void testGetAllWithFilterStock() {
        List<Product> allProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 5),
                new Product(2L, "Product 2", 2L, 2L, 149.99, 10),
                new Product(3L, "Product 3", 1L, 1L, 79.99, 15)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(allProducts);

        ProductFilter filter = new ProductFilter(null, null, null, null, 10);
        List<Product> result = productService.getAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getId).containsExactly(2L, 3L);
        assertThat(result).extracting(Product::getStock).allSatisfy(stock ->
                assertThat(stock).isGreaterThanOrEqualTo(10)
        );
    }

    @Test
    void testGetAllWithComplexFilter() {
        List<Product> allProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(2L, "Product 2", 1L, 2L, 149.99, 5),
                new Product(3L, "Product 3", 2L, 1L, 79.99, 15),
                new Product(4L, "Product 4", 1L, 1L, 129.99, 8)
        );

        when(cacheService.get("ALL_PRODUCTS")).thenReturn(allProducts);

        ProductFilter filter = new ProductFilter(1L, 1L, 80.0, 130.0, 5);
        List<Product> result = productService.getAll(filter);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getId).containsExactly(1L, 4L);
    }

    @Test
    void testGetByIdWithCache() {
        Product expectedProduct = new Product(1L, "Test Product", 1L, 1L, 99.99, 10);

        when(cacheService.get("PRODUCT1")).thenReturn(null);
        when(productRepository.getById(1L)).thenReturn(Optional.of(expectedProduct));

        Product result = productService.getById(1L);

        assertThat(result).isEqualTo(expectedProduct);
        verify(cacheService).put("PRODUCT1", expectedProduct);
        verify(auditService).logAction(AuditLog.AuditActions.VIEW_PRODUCT, "Просмотрен товар ID: 1");
    }

    @Test
    void testGetByIdFromCache() {
        Product cachedProduct = new Product(1L, "Cached Product", 1L, 1L, 99.99, 10);

        when(cacheService.get("PRODUCT1")).thenReturn(cachedProduct);

        Product result = productService.getById(1L);

        assertThat(result).isEqualTo(cachedProduct);
        verify(productRepository, never()).getById(any());
        verify(auditService).logAction(AuditLog.AuditActions.VIEW_PRODUCT, "Просмотрен товар ID: 1");
    }

    @Test
    void testGetByIdNotFound() {
        when(cacheService.get("PRODUCT1")).thenReturn(null);
        when(productRepository.getById(1L)).thenReturn(Optional.empty());

        Product result = productService.getById(1L);

        assertThat(result).isNull();
        verify(cacheService, never()).put(any(), any());
        verify(auditService).logAction(AuditLog.AuditActions.VIEW_PRODUCT, "Просмотрен товар ID: 1");
    }

    @Test
    void testSave() {
        Product productToSave = new Product("New Product", 1L, 1L, 99.99, 10);
        Product savedProduct = new Product(1L, "New Product", 1L, 1L, 99.99, 10);

        when(productRepository.save(productToSave)).thenReturn(savedProduct);

        Product result = productService.save(productToSave);

        assertThat(result).isEqualTo(savedProduct);
        verify(cacheService).invalidate("ALL_PRODUCTS");
        verify(auditService).logAction(AuditLog.AuditActions.ADD_PRODUCT, "Добавлен товар: New Product");
    }

    @Test
    void testUpdateSuccess() {
        Product existingProduct = new Product(1L, "Old Product", 1L, 1L, 99.99, 10);
        Product sourceProduct = new Product("Updated Product", 2L, 2L, 149.99, 5);
        Product updatedProduct = new Product(1L, "Updated Product", 2L, 2L, 149.99, 5);

        when(cacheService.get("PRODUCT1")).thenReturn(existingProduct);
        when(productRepository.update(existingProduct)).thenReturn(updatedProduct);

        Product result = productService.update(1L, sourceProduct);

        assertThat(result).isEqualTo(updatedProduct);
        assertThat(existingProduct.getName()).isEqualTo("Updated Product");
        assertThat(existingProduct.getCategoryId()).isEqualTo(2L);
        assertThat(existingProduct.getBrandId()).isEqualTo(2L);
        assertThat(existingProduct.getPrice()).isEqualTo(149.99);
        assertThat(existingProduct.getStock()).isEqualTo(5);

        verify(cacheService).invalidate("PRODUCT1");
        verify(cacheService).invalidate("ALL_PRODUCTS");
        verify(auditService).logAction(AuditLog.AuditActions.UPDATE_PRODUCT, "Обновлен товар ID: 1");
    }

    @Test
    void testUpdateNotFound() {
        Product sourceProduct = new Product("Updated Product", 2L, 2L, 149.99, 5);

        when(cacheService.get("PRODUCT1")).thenReturn(null);
        when(productRepository.getById(1L)).thenReturn(Optional.empty());

        Product result = productService.update(1L, sourceProduct);

        assertThat(result).isNull();
        verify(productRepository, never()).update(any());
        verify(cacheService, never()).invalidate(any());
        verify(auditService, never()).logAction(eq(AuditLog.AuditActions.UPDATE_PRODUCT), anyString());
    }

    @Test
    void testDeleteByIdSuccess() {
        when(productRepository.deleteById(1L)).thenReturn(true);

        boolean result = productService.deleteById(1L);

        assertThat(result).isTrue();
        verify(cacheService).invalidate("PRODUCT1");
        verify(cacheService).invalidate("ALL_PRODUCTS");
        verify(auditService).logAction(AuditLog.AuditActions.DELETE_PRODUCT, "Удален товар ID: 1");
    }

    @Test
    void testDeleteByIdFailure() {
        when(productRepository.deleteById(1L)).thenReturn(false);

        boolean result = productService.deleteById(1L);

        assertThat(result).isFalse();
        verify(cacheService, never()).invalidate(any());
        verify(auditService, never()).logAction(eq(AuditLog.AuditActions.DELETE_PRODUCT), anyString());
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
    void testCacheInvalidationOnUpdate() {
        Product existingProduct = new Product(1L, "Old Product", 1L, 1L, 99.99, 10);
        Product sourceProduct = new Product("Updated Product", 2L, 2L, 149.99, 5);
        Product updatedProduct = new Product(1L, "Updated Product", 2L, 2L, 149.99, 5);

        when(cacheService.get("PRODUCT1")).thenReturn(existingProduct);
        when(productRepository.update(existingProduct)).thenReturn(updatedProduct);

        productService.update(1L, sourceProduct);

        verify(cacheService).invalidate("PRODUCT1");
        verify(cacheService).invalidate("ALL_PRODUCTS");
    }

    @Test
    void testCacheInvalidationOnDelete() {
        when(productRepository.deleteById(1L)).thenReturn(true);

        productService.deleteById(1L);

        verify(cacheService).invalidate("PRODUCT1");
        verify(cacheService).invalidate("ALL_PRODUCTS");
    }
}
