package com.my.service.impl;

import com.my.InstancioTestEntityFactory;
import com.my.UserManagerMockHelper;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityHasReferencesException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.repository.CategoryRepository;
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
@DisplayName("Тесты сервиса категорий")
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogValidationService validationService;

    @Mock
    private CacheService cacheService;

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, validationService, cacheService, categoryMapper);
    }

    @Test
    @DisplayName("getAll() - Получение всех категорий (кэш пустой)")
    void whenGetAll_thenReturnCategoriesFromRepositoryAndCache() {
        int countCategories = 6;
        List<Category> expectedCategories = InstancioTestEntityFactory.createCategoryList(countCategories);
        String cacheKey = CacheKeyGenerator.generateAllCategoriesKey();

        when(categoryRepository.findAll()).thenReturn(expectedCategories);
        when(cacheService.getList(cacheKey, Category.class)).thenReturn(null);

        List<Category> result = categoryService.getAll();

        assertThat(result)
                .hasSize(countCategories)
                .isEqualTo(expectedCategories);
        verify(cacheService).getList(cacheKey, Category.class);
        verify(cacheService).put(cacheKey, expectedCategories);
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("getAll() - Получение всех категорий из кэша")
    void whenGetAll_thenReturnCategoriesFromCache() {
        int countCategories = 6;
        List<Category> expectedCategories = InstancioTestEntityFactory.createCategoryList(countCategories);
        String cacheKey = CacheKeyGenerator.generateAllCategoriesKey();

        when(cacheService.getList(cacheKey, Category.class)).thenReturn(expectedCategories);

        List<Category> result = categoryService.getAll();

        assertThat(result)
                .hasSize(countCategories)
                .isEqualTo(expectedCategories);
        verify(cacheService).getList(cacheKey, Category.class);
        verify(cacheService, never()).put(anyString(), any());
        verify(categoryRepository, never()).findAll();
    }

    @Test
    @DisplayName("getById() - Получение категории по ID (кэш пустой)")
    void whenGetExistingCategoryById_thenReturnCategoryFromRepositoryAndCache() {
        Long categoryId = 1L;
        Category expectedCategory = InstancioTestEntityFactory.createCategory(categoryId);
        String cacheKey = CacheKeyGenerator.generateCategoryKey(categoryId);

        when(cacheService.get(cacheKey, Category.class)).thenReturn(null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(expectedCategory));

        Category result = categoryService.getById(categoryId);

        assertThat(result).isEqualTo(expectedCategory);
        verify(cacheService).get(cacheKey, Category.class);
        verify(cacheService).put(cacheKey, expectedCategory);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("getById() - Получение категории по ID из кэша")
    void whenGetExistingCategoryById_thenReturnCategoryFromCache() {
        Long categoryId = 1L;
        Category expectedCategory = InstancioTestEntityFactory.createCategory(categoryId);
        String cacheKey = CacheKeyGenerator.generateCategoryKey(categoryId);

        when(cacheService.get(cacheKey, Category.class)).thenReturn(expectedCategory);

        Category result = categoryService.getById(categoryId);

        assertThat(result).isEqualTo(expectedCategory);
        verify(cacheService).get(cacheKey, Category.class);
        verify(cacheService, never()).put(anyString(), any());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getById() - Попытка получения несуществующей категории")
    void whenGetNonExistingCategoryById_thenThrowException() {
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(categoryId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Категория с id " + categoryId + " не найдена");
    }

    @Test
    @DisplayName("save() - Сохранение новой категории")
    void whenSaveNewCategory_thenReturnSavedCategoryAndInvalidateCache() {
        UserManagerMockHelper.setAdminUser();
        Category newCategory = new Category("Sports");
        Category savedCategory = new Category(1L, "Sports");
        String cacheKey = CacheKeyGenerator.generateAllCategoriesKey();

        when(categoryRepository.existsByNameIgnoreCase("Sports")).thenReturn(false);
        when(categoryRepository.save(newCategory)).thenReturn(savedCategory);

        Category result = categoryService.save(newCategory);

        assertThat(result).isEqualTo(savedCategory);
        verify(categoryRepository).save(newCategory);
        verify(cacheService).invalidate(cacheKey);
    }

    @Test
    @DisplayName("save() - Попытка сохранения существующей категории")
    void whenSaveExistingCategory_thenThrowException() {
        UserManagerMockHelper.setAdminUser();
        Category existingCategory = new Category("Electronics");
        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.save(existingCategory))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Electronics уже существует");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("update() - Обновление категории")
    void whenUpdateCategory_thenReturnUpdatedCategoryAndInvalidateCache() {
        UserManagerMockHelper.setAdminUser();
        Long categoryId = 1L;
        Category sourceCategory = new Category("Updated Electronics");
        Category existingCategory = new Category(categoryId, "Electronics");
        Category updatedCategory = new Category(categoryId, "Updated Electronics");
        String categoryCacheKey = CacheKeyGenerator.generateCategoryKey(categoryId);
        String allCategoriesCacheKey = CacheKeyGenerator.generateAllCategoriesKey();

        when(cacheService.get(categoryCacheKey, Category.class)).thenReturn(existingCategory);
        when(categoryRepository.existsByNameIgnoreCase("Updated Electronics")).thenReturn(false);
        when(categoryRepository.update(existingCategory)).thenReturn(updatedCategory);

        Category result = categoryService.update(categoryId, sourceCategory);

        assertThat(result).isEqualTo(updatedCategory);
        verify(categoryMapper).updateCategory(sourceCategory, existingCategory);
        verify(cacheService).invalidate(categoryCacheKey);
        verify(cacheService).invalidate(allCategoriesCacheKey);
    }

    @Test
    @DisplayName("update() - Попытка обновления категории на существующее имя")
    void whenUpdateCategoryWithExistingName_thenThrowException() {
        UserManagerMockHelper.setAdminUser();
        Long categoryId = 1L;
        Category sourceCategory = new Category("Clothing");
        Category existingCategory = new Category(categoryId, "Electronics");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameIgnoreCase("Clothing")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(categoryId, sourceCategory))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Clothing уже существует");

        verify(categoryRepository, never()).update(any());
    }

    @Test
    @DisplayName("deleteById() - Удаление категории без связанных товаров")
    void whenDeleteCategoryWithoutProducts_thenReturnTrueAndInvalidateCache() {
        UserManagerMockHelper.setAdminUser();
        Long categoryId = 1L;
        String categoryCacheKey = CacheKeyGenerator.generateCategoryKey(categoryId);
        String allCategoriesCacheKey = CacheKeyGenerator.generateAllCategoriesKey();

        when(validationService.categoryHasProducts(categoryId)).thenReturn(false);
        when(categoryRepository.deleteById(categoryId)).thenReturn(true);

        boolean result = categoryService.deleteById(categoryId);

        assertThat(result).isTrue();
        verify(categoryRepository).deleteById(categoryId);
        verify(cacheService).invalidate(categoryCacheKey);
        verify(cacheService).invalidate(allCategoriesCacheKey);
    }

    @Test
    @DisplayName("deleteById() - Неудачное удаление категории")
    void whenDeleteCategoryFails_thenReturnFalseAndDoNotInvalidateCache() {
        UserManagerMockHelper.setAdminUser();
        Long categoryId = 1L;

        when(validationService.categoryHasProducts(categoryId)).thenReturn(false);
        when(categoryRepository.deleteById(categoryId)).thenReturn(false);

        boolean result = categoryService.deleteById(categoryId);

        assertThat(result).isFalse();
        verify(categoryRepository).deleteById(categoryId);
        verify(cacheService, never()).invalidate(anyString());
    }

    @Test
    @DisplayName("deleteById() - Попытка удаления категории с связанными товарами")
    void whenDeleteCategoryWithProducts_thenReturnFalse() {
        UserManagerMockHelper.setAdminUser();
        Long categoryId = 1L;
        when(validationService.categoryHasProducts(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteById(categoryId))
                .isInstanceOf(EntityHasReferencesException.class)
                .hasMessageContaining("Невозможно удалить категорию с ID=" + categoryId);

        verify(categoryRepository, never()).deleteById(any());
        verify(cacheService, never()).invalidate(anyString());
    }


    @Test
    @DisplayName("existsByName() - Проверка существования категории по имени")
    void whenExistsByName_thenDelegateToRepository() {
        String categoryName = "Electronics";
        when(categoryRepository.existsByNameIgnoreCase(categoryName)).thenReturn(true);

        boolean result = categoryService.existsByName(categoryName);

        assertThat(result).isTrue();
        verify(categoryRepository).existsByNameIgnoreCase(categoryName);
    }
}
