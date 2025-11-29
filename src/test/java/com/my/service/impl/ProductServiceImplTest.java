package com.my.service.impl;

import com.my.exception.EntityNotFoundException;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.ProductRepository;
import com.my.service.CacheService;
import com.my.util.CacheKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private CacheService cacheService;

    @Mock
    private ProductMapper productMapper;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, cacheService, productMapper);
    }

    @Test
    void whenGetAllWithoutFilters_thenFetchFromRepositoryAndCache() {
        List<Product> expectedProducts = Arrays.asList(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(2L, "Product 2", 1L, 2L, 149.99, 5)
        );
        String cacheKey = CacheKeyGenerator.generateAllProductsKey();

        when(cacheService.getList(cacheKey, Product.class)).thenReturn(null);
        when(productRepository.getAll(null)).thenReturn(expectedProducts);

        List<Product> result = productService.getAll(null);

        assertThat(result).isEqualTo(expectedProducts);
        verify(cacheService).getList(cacheKey, Product.class);
        verify(cacheService).put(cacheKey, expectedProducts);
        verify(productRepository).getAll(null);
    }

    @Test
    void whenGetAllWithoutFilters_thenReturnFromCache() {
        List<Product> expectedProducts = Arrays.asList(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(2L, "Product 2", 1L, 2L, 149.99, 5)
        );
        String cacheKey = CacheKeyGenerator.generateAllProductsKey();

        when(cacheService.getList(cacheKey, Product.class)).thenReturn(expectedProducts);

        List<Product> result = productService.getAll(null);

        assertThat(result).isEqualTo(expectedProducts);
        verify(cacheService).getList(cacheKey, Product.class);
        verify(cacheService, never()).put(anyString(), any());
        verify(productRepository, never()).getAll(any());
    }

    @Test
    void whenGetAllWithFilters_thenFetchFromRepositoryWithoutCache() {
        ProductFilter filter = new ProductFilter(1L, null, null, null, null);
        List<Product> expectedProducts = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10)
        );

        when(productRepository.getAll(filter)).thenReturn(expectedProducts);

        List<Product> result = productService.getAll(filter);

        assertThat(result).isEqualTo(expectedProducts);
        verify(productRepository).getAll(filter);
        verify(cacheService, never()).getList(anyString(), any());
        verify(cacheService, never()).put(anyString(), any());
    }

    @Test
    void whenGetExistingProductById_thenFetchFromRepositoryAndCache() {
        Long productId = 1L;
        Product expectedProduct = new Product(productId, "Product 1", 1L, 1L, 99.99, 10);
        String cacheKey = CacheKeyGenerator.generateProductKey(productId);

        when(cacheService.get(cacheKey, Product.class)).thenReturn(null);
        when(productRepository.getById(productId)).thenReturn(Optional.of(expectedProduct));

        Product result = productService.getById(productId);

        assertThat(result).isEqualTo(expectedProduct);
        verify(cacheService).get(cacheKey, Product.class);
        verify(cacheService).put(cacheKey, expectedProduct);
    }

    @Test
    void whenGetExistingProductById_thenReturnFromCache() {
        Long productId = 1L;
        Product expectedProduct = new Product(productId, "Product 1", 1L, 1L, 99.99, 10);
        String cacheKey = CacheKeyGenerator.generateProductKey(productId);

        when(cacheService.get(cacheKey, Product.class)).thenReturn(expectedProduct);

        Product result = productService.getById(productId);

        assertThat(result).isEqualTo(expectedProduct);
        verify(cacheService).get(cacheKey, Product.class);
        verify(cacheService, never()).put(anyString(), any());
        verify(productRepository, never()).getById(any());
    }

    @Test
    void whenGetNonExistingProductById_thenThrowException() {
        Long productId = 999L;

        when(cacheService.get(anyString(), eq(Product.class))).thenReturn(null);
        when(productRepository.getById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Товар с id " + productId + " не найден");
    }

    @Test
    void whenSaveProduct_thenReturnSavedProductAndInvalidateCache() {
        Product newProduct = new Product("New Product", 1L, 1L, 99.99, 10);
        Product savedProduct = new Product(1L, "New Product", 1L, 1L, 99.99, 10);
        String cacheKey = CacheKeyGenerator.generateAllProductsKey();

        when(productRepository.save(newProduct)).thenReturn(savedProduct);

        Product result = productService.save(newProduct);

        assertThat(result).isEqualTo(savedProduct);
        verify(cacheService).invalidate(cacheKey);
        verify(productRepository).save(newProduct);
    }

    @Test
    void whenUpdateProduct_thenReturnUpdatedProductAndInvalidateCache() {
        Long productId = 1L;
        Product sourceProduct = new Product("Updated Product", 2L, 2L, 199.99, 15);
        Product existingProduct = new Product(productId, "Original Product", 1L, 1L, 99.99, 10);
        Product updatedProduct = new Product(productId, "Updated Product", 2L, 2L, 199.99, 15);
        String productCacheKey = CacheKeyGenerator.generateProductKey(productId);
        String allProductsCacheKey = CacheKeyGenerator.generateAllProductsKey();

        when(cacheService.get(productCacheKey, Product.class)).thenReturn(existingProduct);
        when(productRepository.update(existingProduct)).thenReturn(updatedProduct);

        Product result = productService.update(productId, sourceProduct);

        assertThat(result).isEqualTo(updatedProduct);
        verify(productMapper).updateProduct(sourceProduct, existingProduct);
        verify(cacheService).invalidate(productCacheKey);
        verify(cacheService).invalidate(allProductsCacheKey);
    }

    @Test
    void whenDeleteProductSuccessfully_thenReturnTrueAndInvalidateCache() {
        Long productId = 1L;
        String productCacheKey = CacheKeyGenerator.generateProductKey(productId);
        String allProductsCacheKey = CacheKeyGenerator.generateAllProductsKey();

        when(productRepository.deleteById(productId)).thenReturn(true);

        boolean result = productService.deleteById(productId);

        assertThat(result).isTrue();
        verify(cacheService).invalidate(productCacheKey);
        verify(cacheService).invalidate(allProductsCacheKey);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void whenDeleteProductFails_thenReturnFalseAndDoNotInvalidateCache() {
        Long productId = 1L;
        when(productRepository.deleteById(productId)).thenReturn(false);

        boolean result = productService.deleteById(productId);

        assertThat(result).isFalse();
        verify(cacheService, never()).invalidate(anyString());
        verify(productRepository).deleteById(productId);
    }
}
