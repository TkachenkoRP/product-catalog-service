package com.my.service.impl;

import com.my.InstancioTestEntityFactory;
import com.my.UserManagerMockHelper;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityHasReferencesException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.repository.BrandRepository;
import com.my.service.CacheService;
import com.my.service.CatalogValidationService;
import com.my.util.CacheKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса брендов")
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
    @DisplayName("getAll() - Получение всех брендов (кэш пустой)")
    void whenGetAll_thenReturnBrandsFromRepositoryAndCache() {
        int countBrands = 5;
        List<Brand> expectedBrands = InstancioTestEntityFactory.createBrandList(countBrands);
        String cacheKey = CacheKeyGenerator.generateAllBrandsKey();

        when(cacheService.getList(cacheKey, Brand.class)).thenReturn(null);
        when(brandRepository.findAll()).thenReturn(expectedBrands);

        List<Brand> result = brandService.getAll();

        assertThat(result)
                .hasSize(countBrands)
                .isEqualTo(expectedBrands);
        verify(cacheService).getList(cacheKey, Brand.class);
        verify(cacheService).put(cacheKey, expectedBrands);
        verify(brandRepository).findAll();
    }

    @Test
    @DisplayName("getAll() - Получение всех брендов из кэша")
    void whenGetAll_thenReturnBrandsFromCache() {
        int countBrands = 5;
        List<Brand> expectedBrands = InstancioTestEntityFactory.createBrandList(countBrands);
        String cacheKey = CacheKeyGenerator.generateAllBrandsKey();

        when(cacheService.getList(cacheKey, Brand.class)).thenReturn(expectedBrands);

        List<Brand> result = brandService.getAll();

        assertThat(result)
                .hasSize(countBrands)
                .isEqualTo(expectedBrands);
        verify(cacheService).getList(cacheKey, Brand.class);
        verify(cacheService, never()).put(anyString(), any());
        verify(brandRepository, never()).findAll();
    }

    @Test
    @DisplayName("getById() - Получение бренда по ID (кэш пустой)")
    void whenGetExistingBrandById_thenReturnBrandFromRepositoryAndCache() {
        Long brandId = 1L;
        Brand expectedBrand = InstancioTestEntityFactory.createBrand(brandId);
        String cacheKey = CacheKeyGenerator.generateBrandKey(brandId);

        when(cacheService.get(cacheKey, Brand.class)).thenReturn(null);
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(expectedBrand));

        Brand result = brandService.getById(brandId);

        assertThat(result).isEqualTo(expectedBrand);
        verify(cacheService).get(cacheKey, Brand.class);
        verify(cacheService).put(cacheKey, expectedBrand);
        verify(brandRepository).findById(brandId);
    }

    @Test
    @DisplayName("getById() - Получение бренда по ID из кэша")
    void whenGetExistingBrandById_thenReturnBrandFromCache() {
        Long brandId = 1L;
        Brand expectedBrand = InstancioTestEntityFactory.createBrand(brandId);
        String brandKey = CacheKeyGenerator.generateBrandKey(brandId);

        when(cacheService.get(brandKey, Brand.class)).thenReturn(expectedBrand);

        Brand result = brandService.getById(brandId);

        assertThat(result).isEqualTo(expectedBrand);
        verify(cacheService).get(brandKey, Brand.class);
        verify(cacheService, never()).put(anyString(), any());
        verify(brandRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getById() - Попытка получения несуществующего бренда")
    void whenGetNonExistingBrandById_thenThrowException() {
        Long brandId = 999L;
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.getById(brandId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Бренд с id " + brandId + " не найден");
    }

    @Test
    @DisplayName("save() - Сохранение нового бренда")
    void whenSaveNewBrand_thenReturnSavedBrandAndInvalidateCache() {
        UserManagerMockHelper.setAdminUser();
        Brand newBrand = new Brand("Sony");
        Brand savedBrand = new Brand(1L, "Sony");
        String cacheKey = CacheKeyGenerator.generateAllBrandsKey();

        when(brandRepository.existsByNameIgnoreCase("Sony")).thenReturn(false);
        when(brandRepository.save(newBrand)).thenReturn(savedBrand);

        Brand result = brandService.save(newBrand);

        assertThat(result).isEqualTo(savedBrand);
        verify(brandRepository).save(newBrand);
        verify(cacheService).invalidate(cacheKey);
    }

    @Test
    @DisplayName("save() - Попытка сохранения существующего бренда")
    void whenSaveExistingBrand_thenThrowException() {
        UserManagerMockHelper.setAdminUser();
        Brand existingBrand = new Brand("Samsung");
        when(brandRepository.existsByNameIgnoreCase("Samsung")).thenReturn(true);

        assertThatThrownBy(() -> brandService.save(existingBrand))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Samsung уже существует");

        verify(brandRepository, never()).save(any());
    }

    @Test
    @DisplayName("update() - Обновление бренда")
    void whenUpdateBrand_thenReturnUpdatedBrandAndInvalidateCache() {
        UserManagerMockHelper.setAdminUser();
        Long brandId = 1L;
        Brand sourceBrand = new Brand("Updated Samsung");
        Brand existingBrand = new Brand(brandId, "Samsung");
        Brand updatedBrand = new Brand(brandId, "Updated Samsung");
        String brandCacheKey = CacheKeyGenerator.generateBrandKey(brandId);
        String allBrandsCacheKey = CacheKeyGenerator.generateAllBrandsKey();

        when(cacheService.get(brandCacheKey, Brand.class)).thenReturn(existingBrand);
        when(brandRepository.existsByNameIgnoreCase("Updated Samsung")).thenReturn(false);
        when(brandRepository.update(existingBrand)).thenReturn(updatedBrand);

        Brand result = brandService.update(brandId, sourceBrand);

        assertThat(result).isEqualTo(updatedBrand);
        verify(brandMapper).updateBrand(sourceBrand, existingBrand);
        verify(cacheService).invalidate(brandCacheKey);
        verify(cacheService).invalidate(allBrandsCacheKey);
    }

    @Test
    @DisplayName("deleteById() - Удаление бренда без связанных товаров")
    void whenDeleteBrandWithoutProducts_thenReturnTrueAndInvalidateCache() {
        UserManagerMockHelper.setAdminUser();
        Long brandId = 1L;
        String brandCacheKey = CacheKeyGenerator.generateBrandKey(brandId);
        String allBrandsCacheKey = CacheKeyGenerator.generateAllBrandsKey();

        when(validationService.brandHasProducts(brandId)).thenReturn(false);
        when(brandRepository.deleteById(brandId)).thenReturn(true);

        boolean result = brandService.deleteById(brandId);

        assertThat(result).isTrue();
        verify(brandRepository).deleteById(brandId);
        verify(cacheService).invalidate(brandCacheKey);
        verify(cacheService).invalidate(allBrandsCacheKey);
    }

    @Test
    @DisplayName("deleteById() - Попытка удаления бренда с связанными товарами")
    void whenDeleteBrandWithProducts_thenReturnFalse() {
        UserManagerMockHelper.setAdminUser();
        Long brandId = 1L;
        when(validationService.brandHasProducts(brandId)).thenReturn(true);

        assertThatThrownBy(() -> brandService.deleteById(brandId))
                .isInstanceOf(EntityHasReferencesException.class)
                .hasMessageContaining("Невозможно удалить бренд с ID=" + brandId);

        verify(brandRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("existsByName() - Проверка существования бренда по имени")
    void whenExistsByName_thenDelegateToRepository() {
        String brandName = "Samsung";
        when(brandRepository.existsByNameIgnoreCase(brandName)).thenReturn(true);

        boolean result = brandService.existsByName(brandName);

        assertThat(result).isTrue();
        verify(brandRepository).existsByNameIgnoreCase(brandName);
    }
}
