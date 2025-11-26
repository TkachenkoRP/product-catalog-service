package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.model.Brand;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.BrandRepository;
import com.my.service.BrandService;
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
    private ProductService productService;

    private BrandService brandService;

    @BeforeEach
    void setUp() {
        brandService = new BrandServiceImpl(brandRepository, productService);
    }

    @Test
    void testGetAll() {
        List<Brand> expectedBrands = List.of(
                new Brand(1L, "Brand 1"),
                new Brand(2L, "Brand 2")
        );

        when(brandRepository.getAll()).thenReturn(expectedBrands);

        List<Brand> result = brandService.getAll();

        assertThat(result).isEqualTo(expectedBrands);
    }

    @Test
    void testGetById() {
        Brand expectedBrand = new Brand(1L, "Test Brand");

        when(brandRepository.getById(1L)).thenReturn(Optional.of(expectedBrand));

        Brand result = brandService.getById(1L);

        assertThat(result).isEqualTo(expectedBrand);
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
    }

    @Test
    void testDeleteByIdWithProducts() {
        Product product = new Product();
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of(product));

        boolean result = brandService.deleteById(1L);

        assertThat(result).isFalse();
        verify(brandRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteByIdRepositoryFailure() {
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of());
        when(brandRepository.deleteById(1L)).thenReturn(false);

        boolean result = brandService.deleteById(1L);

        assertThat(result).isFalse();
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
}