package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.model.Brand;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.BrandRepository;
import com.my.service.BrandService;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private ProductService productService;

    private BrandService brandService;

    @BeforeEach
    void setUp() {
        brandService = new BrandServiceImpl(brandRepository, cacheService, productService);
    }

    @Test
    void testGetAllWithCache() {
        List<Brand> expectedBrands = List.of(
                new Brand(1L, "Brand 1"),
                new Brand(2L, "Brand 2")
        );

        when(cacheService.get("ALL_BRANDS")).thenReturn(null);
        when(brandRepository.getAll()).thenReturn(expectedBrands);

        List<Brand> result = brandService.getAll();

        assertThat(result).isEqualTo(expectedBrands);
        verify(cacheService).put("ALL_BRANDS", expectedBrands);
    }

    @Test
    void testGetAllFromCache() {
        List<Brand> cachedBrands = List.of(
                new Brand(1L, "Cached Brand 1"),
                new Brand(2L, "Cached Brand 2")
        );

        when(cacheService.get("ALL_BRANDS")).thenReturn(cachedBrands);

        List<Brand> result = brandService.getAll();

        assertThat(result).isEqualTo(cachedBrands);
        verify(brandRepository, never()).getAll();
    }

    @Test
    void testGetByIdWithCache() {
        Brand expectedBrand = new Brand(1L, "Test Brand");

        when(cacheService.get("BRAND1")).thenReturn(null);
        when(brandRepository.getById(1L)).thenReturn(Optional.of(expectedBrand));

        Brand result = brandService.getById(1L);

        assertThat(result).isEqualTo(expectedBrand);
        verify(cacheService).put("BRAND1", expectedBrand);
    }

    @Test
    void testGetByIdFromCache() {
        Brand cachedBrand = new Brand(1L, "Cached Brand");

        when(cacheService.get("BRAND1")).thenReturn(cachedBrand);

        Brand result = brandService.getById(1L);

        assertThat(result).isEqualTo(cachedBrand);
        verify(brandRepository, never()).getById(any());
    }

    @Test
    void testGetByIdNotFound() {
        when(brandRepository.getById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.getById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Бренд с id 1 не найден");
    }

    @Test
    void testSaveSuccess() {
        Brand brandToSave = new Brand("New Brand");
        Brand savedBrand = new Brand(1L, "New Brand");

        when(brandRepository.existsByNameIgnoreCase("New Brand")).thenReturn(false);
        when(brandRepository.save(brandToSave)).thenReturn(savedBrand);

        Brand result = brandService.save(brandToSave);

        assertThat(result).isEqualTo(savedBrand);
        verify(cacheService).invalidate("ALL_BRANDS");
    }

    @Test
    void testSaveWithExistingName() {
        Brand brandToSave = new Brand("Existing Brand");

        when(brandRepository.existsByNameIgnoreCase("Existing Brand")).thenReturn(true);

        assertThatThrownBy(() -> brandService.save(brandToSave))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Existing Brand уже существует");
    }

    @Test
    void testUpdateSuccess() {
        Brand existingBrand = new Brand(1L, "Old Brand");
        Brand sourceBrand = new Brand("Updated Brand");
        Brand updatedBrand = new Brand(1L, "Updated Brand");

        when(brandRepository.getById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.existsByNameIgnoreCase("Updated Brand")).thenReturn(false);
        when(brandRepository.update(existingBrand)).thenReturn(updatedBrand);

        Brand result = brandService.update(1L, sourceBrand);

        assertThat(result).isEqualTo(updatedBrand);
        assertThat(existingBrand.getName()).isEqualTo("Updated Brand");
        verify(cacheService).invalidate("BRAND1");
        verify(cacheService).invalidate("ALL_BRANDS");
    }

    @Test
    void testUpdateNotFound() {
        Brand sourceBrand = new Brand("Updated Brand");

        when(brandRepository.getById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.update(1L, sourceBrand))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Бренд с id 1 не найден");
    }

    @Test
    void testUpdateWithExistingName() {
        Brand existingBrand = new Brand(1L, "Old Brand");
        Brand sourceBrand = new Brand("Existing Brand");

        when(brandRepository.getById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.existsByNameIgnoreCase("Existing Brand")).thenReturn(true);

        assertThatThrownBy(() -> brandService.update(1L, sourceBrand))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Existing Brand уже существует");
    }

    @Test
    void testDeleteByIdSuccess() {
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of());
        when(brandRepository.deleteById(1L)).thenReturn(true);

        boolean result = brandService.deleteById(1L);

        assertThat(result).isTrue();
        verify(cacheService).invalidate("BRAND1");
        verify(cacheService).invalidate("ALL_BRANDS");
    }

    @Test
    void testDeleteByIdWithProducts() {
        Product product = new Product();
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of(product));

        boolean result = brandService.deleteById(1L);

        assertThat(result).isFalse();
        verify(brandRepository, never()).deleteById(any());
        verify(cacheService, never()).invalidate(any());
    }

    @Test
    void testDeleteByIdRepositoryFailure() {
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of());
        when(brandRepository.deleteById(1L)).thenReturn(false);

        boolean result = brandService.deleteById(1L);

        assertThat(result).isFalse();
        verify(cacheService, never()).invalidate(any());
    }

    @Test
    void testExistsByName() {
        when(brandRepository.existsByNameIgnoreCase("Test Brand")).thenReturn(true);

        boolean result = brandService.existsByName("Test Brand");

        assertThat(result).isTrue();
        verify(brandRepository).existsByNameIgnoreCase("Test Brand");
    }

    @Test
    void testHasProductsWithBrand() {
        Product product = new Product();
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of(product));

        boolean result = brandService.deleteById(1L);

        assertThat(result).isFalse();
        verify(productService).getAll(argThat(filter ->
                filter != null && filter.brandId().equals(1L)
        ));
    }

    @Test
    void testCacheInvalidationOnSave() {
        Brand brand = new Brand("New Brand");
        Brand savedBrand = new Brand(1L, "New Brand");

        when(brandRepository.existsByNameIgnoreCase("New Brand")).thenReturn(false);
        when(brandRepository.save(brand)).thenReturn(savedBrand);

        brandService.save(brand);

        verify(cacheService).invalidate("ALL_BRANDS");
        verify(cacheService, never()).invalidate("BRAND1");
    }

    @Test
    void testCacheInvalidationOnUpdate() {
        Brand existingBrand = new Brand(1L, "Old Brand");
        Brand sourceBrand = new Brand("Updated Brand");
        Brand updatedBrand = new Brand(1L, "Updated Brand");

        when(brandRepository.getById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.existsByNameIgnoreCase("Updated Brand")).thenReturn(false);
        when(brandRepository.update(existingBrand)).thenReturn(updatedBrand);

        brandService.update(1L, sourceBrand);

        verify(cacheService).invalidate("BRAND1");
        verify(cacheService).invalidate("ALL_BRANDS");
    }
}