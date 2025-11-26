package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.model.Category;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.CategoryRepository;
import com.my.service.CategoryService;
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
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductService productService;

    @Mock
    private RedisCacheServiceImpl redisCacheService;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, productService, redisCacheService);
    }

    @Test
    void testGetAll() {
        List<Category> expectedCategories = List.of(
                new Category(1L, "Category 1"),
                new Category(2L, "Category 2")
        );

        when(categoryRepository.getAll()).thenReturn(expectedCategories);
        when(redisCacheService.getList(any(), any())).thenReturn(null);

        List<Category> result = categoryService.getAll();

        assertThat(result).isEqualTo(expectedCategories);
    }

    @Test
    void testGetByIdNotFound() {
        when(categoryRepository.getById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Категория с id 1 не найдена");
    }

    @Test
    void testSaveSuccess() {
        Category categoryToSave = new Category("New Category");
        Category savedCategory = new Category(1L, "New Category");

        when(categoryRepository.existsByNameIgnoreCase("New Category")).thenReturn(false);
        when(categoryRepository.save(categoryToSave)).thenReturn(savedCategory);

        Category result = categoryService.save(categoryToSave);

        assertThat(result).isEqualTo(savedCategory);
    }

    @Test
    void testSaveWithExistingName() {
        Category categoryToSave = new Category("Existing Category");

        when(categoryRepository.existsByNameIgnoreCase("Existing Category")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.save(categoryToSave))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Existing Category уже существует");
    }

    @Test
    void testUpdateSuccess() {
        Category existingCategory = new Category(1L, "Old Category");
        Category sourceCategory = new Category("Updated Category");
        Category updatedCategory = new Category(1L, "Updated Category");

        when(categoryRepository.getById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameIgnoreCase("Updated Category")).thenReturn(false);
        when(categoryRepository.update(existingCategory)).thenReturn(updatedCategory);

        Category result = categoryService.update(1L, sourceCategory);

        assertThat(result).isEqualTo(updatedCategory);
        assertThat(existingCategory.getName()).isEqualTo("Updated Category");
    }

    @Test
    void testUpdateNotFound() {
        Category sourceCategory = new Category("Updated Category");

        when(categoryRepository.getById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(1L, sourceCategory))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Категория с id 1 не найдена");
    }

    @Test
    void testDeleteByIdSuccess() {
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of());
        when(categoryRepository.deleteById(1L)).thenReturn(true);

        boolean result = categoryService.deleteById(1L);

        assertThat(result).isTrue();
    }

    @Test
    void testDeleteByIdWithProducts() {
        Product product = new Product();
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of(product));

        boolean result = categoryService.deleteById(1L);

        assertThat(result).isFalse();
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteByIdRepositoryFailure() {
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of());
        when(categoryRepository.deleteById(1L)).thenReturn(false);

        boolean result = categoryService.deleteById(1L);

        assertThat(result).isFalse();
    }

    @Test
    void testExistsByName() {
        when(categoryRepository.existsByNameIgnoreCase("Test Category")).thenReturn(true);

        boolean result = categoryService.existsByName("Test Category");

        assertThat(result).isTrue();
        verify(categoryRepository).existsByNameIgnoreCase("Test Category");
    }

    @Test
    void testHasProductsWithCategory() {
        Product product = new Product();
        when(productService.getAll(any(ProductFilter.class))).thenReturn(List.of(product));

        boolean result = categoryService.deleteById(1L);

        assertThat(result).isFalse();
        verify(productService).getAll(argThat(filter ->
                filter != null && filter.categoryId().equals(1L)
        ));
    }

    @Test
    void testBusinessLogicIntegration() {
        Category category = new Category("Test Category");
        Category savedCategory = new Category(1L, "Test Category");

        when(categoryRepository.existsByNameIgnoreCase("Test Category")).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(savedCategory);

        Category result = categoryService.save(category);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Category");
    }
}
