package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityHasReferencesException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.repository.BrandRepository;
import com.my.service.CacheService;
import com.my.service.CatalogValidationService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {
    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CatalogValidationService validationService;

    @Mock
    private CacheService cacheService;

    @Mock
    private BrandMapper brandMapper;

    private BrandServiceImpl brandService;

    @BeforeEach
    void setUp() {
        brandService = new BrandServiceImpl(brandRepository, validationService, cacheService, brandMapper);
    }

    @Test
    void whenGetAll_thenReturnBrandsFromRepository() {
        List<Brand> expectedBrands = Arrays.asList(
                new Brand(1L, "Samsung"),
                new Brand(2L, "Apple")
        );
        when(brandRepository.getAll()).thenReturn(expectedBrands);
        when(cacheService.getList(any(),any())).thenReturn(null);

        List<Brand> result = brandService.getAll();

        assertThat(result).isEqualTo(expectedBrands);
        verify(brandRepository).getAll();
    }

    @Test
    void whenGetExistingBrandById_thenReturnBrand() {
        Long brandId = 1L;
        Brand expectedBrand = new Brand(brandId, "Samsung");
        when(brandRepository.getById(brandId)).thenReturn(Optional.of(expectedBrand));

        Brand result = brandService.getById(brandId);

        assertThat(result).isEqualTo(expectedBrand);
    }

    @Test
    void whenGetNonExistingBrandById_thenThrowException() {
        Long brandId = 999L;
        when(brandRepository.getById(brandId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.getById(brandId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Бренд с id " + brandId + " не найден");
    }

    @Test
    void whenSaveNewBrand_thenReturnSavedBrand() {
        Brand newBrand = new Brand("Sony");
        Brand savedBrand = new Brand(1L, "Sony");

        when(brandRepository.existsByNameIgnoreCase("Sony")).thenReturn(false);
        when(brandRepository.save(newBrand)).thenReturn(savedBrand);

        Brand result = brandService.save(newBrand);

        assertThat(result).isEqualTo(savedBrand);
        verify(brandRepository).save(newBrand);
    }

    @Test
    void whenSaveExistingBrand_thenThrowException() {
        Brand existingBrand = new Brand("Samsung");
        when(brandRepository.existsByNameIgnoreCase("Samsung")).thenReturn(true);

        assertThatThrownBy(() -> brandService.save(existingBrand))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Samsung уже существует");

        verify(brandRepository, never()).save(any());
    }

    @Test
    void whenUpdateBrand_thenReturnUpdatedBrand() {
        Long brandId = 1L;
        Brand sourceBrand = new Brand("Updated Samsung");
        Brand existingBrand = new Brand(brandId, "Samsung");
        Brand updatedBrand = new Brand(brandId, "Updated Samsung");

        when(brandRepository.getById(brandId)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.existsByNameIgnoreCase("Updated Samsung")).thenReturn(false);
        when(brandRepository.update(existingBrand)).thenReturn(updatedBrand);

        Brand result = brandService.update(brandId, sourceBrand);

        assertThat(result).isEqualTo(updatedBrand);
        verify(brandMapper).updateBrand(sourceBrand, existingBrand);
    }

    @Test
    void whenDeleteBrandWithoutProducts_thenReturnTrue() {
        Long brandId = 1L;
        when(validationService.brandHasProducts(brandId)).thenReturn(false);
        when(brandRepository.deleteById(brandId)).thenReturn(true);

        boolean result = brandService.deleteById(brandId);

        assertThat(result).isTrue();
        verify(brandRepository).deleteById(brandId);
    }

    @Test
    void whenDeleteBrandWithProducts_thenReturnFalse() {
        Long brandId = 1L;
        when(validationService.brandHasProducts(brandId)).thenReturn(true);

        assertThatThrownBy(() -> brandService.deleteById(brandId))
                .isInstanceOf(EntityHasReferencesException.class)
                .hasMessageContaining("Невозможно удалить бренд с ID=" + brandId);

        verify(brandRepository, never()).deleteById(any());
    }

    @Test
    void whenExistsByName_thenDelegateToRepository() {
        String brandName = "Samsung";
        when(brandRepository.existsByNameIgnoreCase(brandName)).thenReturn(true);

        boolean result = brandService.existsByName(brandName);

        assertThat(result).isTrue();
        verify(brandRepository).existsByNameIgnoreCase(brandName);
    }
}
