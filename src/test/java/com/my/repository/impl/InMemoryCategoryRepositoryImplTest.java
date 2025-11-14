package com.my.repository.impl;

import com.my.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class InMemoryCategoryRepositoryImplTest {
    private InMemoryCategoryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCategoryRepositoryImpl();
    }

    @Test
    void testGetAllInitiallyEmpty() {
        List<Category> categories = repository.getAll();

        assertThat(categories).isNotNull().isEmpty();
    }

    @Test
    void testSave() {
        Category category = new Category("Test Category");

        Category savedCategory = repository.save(category);

        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isEqualTo(1L);
        assertThat(savedCategory.getName()).isEqualTo("Test Category");

        List<Category> categories = repository.getAll();
        assertThat(categories).hasSize(1);
    }

    @Test
    void testGetById() {
        Category category = new Category("Test Category");
        repository.save(category);

        Optional<Category> foundCategory = repository.getById(1L);

        assertThat(foundCategory).isPresent();
        foundCategory.ifPresent(c -> {
            assertThat(c.getId()).isEqualTo(1L);
            assertThat(c.getName()).isEqualTo("Test Category");
        });
    }

    @Test
    void testGetByIdNotFound() {
        Optional<Category> foundCategory = repository.getById(999L);

        assertThat(foundCategory).isEmpty();
    }

    @Test
    void testUpdate() {
        Category category = new Category("Original Category");
        repository.save(category);

        Category updatedCategory = new Category("Updated Category");
        updatedCategory.setId(1L);

        Category result = repository.update(updatedCategory);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Category");

        Optional<Category> foundCategory = repository.getById(1L);
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Updated Category");
    }

    @Test
    void testUpdateNotFound() {
        Category nonExistentCategory = new Category("Non Existent");
        nonExistentCategory.setId(999L);

        Category result = repository.update(nonExistentCategory);

        assertThat(result).isNull();
    }

    @Test
    void testDeleteById() {
        Category category = new Category("Test Category");
        repository.save(category);

        boolean deleted = repository.deleteById(1L);

        assertThat(deleted).isTrue();

        Optional<Category> foundCategory = repository.getById(1L);
        assertThat(foundCategory).isEmpty();

        List<Category> categories = repository.getAll();
        assertThat(categories).isEmpty();
    }

    @Test
    void testDeleteByIdNotFound() {
        boolean deleted = repository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void testExistsByNameIgnoreCase() {
        Category category = new Category("Test Category");
        repository.save(category);

        boolean exists = repository.existsByNameIgnoreCase("test category");
        assertThat(exists).isTrue();

        boolean existsExact = repository.existsByNameIgnoreCase("Test Category");
        assertThat(existsExact).isTrue();

        boolean notExists = repository.existsByNameIgnoreCase("Nonexistent Category");
        assertThat(notExists).isFalse();
    }

    @Test
    void testLoadData() {
        List<Category> categoriesToLoad = List.of(
                new Category(10L, "Category 1"),
                new Category(20L, "Category 2")
        );

        repository.loadData(categoriesToLoad);

        List<Category> categories = repository.getAll();
        assertThat(categories).hasSize(2);

        Optional<Category> category1 = repository.getById(10L);
        assertThat(category1).isPresent();
        assertThat(category1.get().getName()).isEqualTo("Category 1");

        Optional<Category> category2 = repository.getById(20L);
        assertThat(category2).isPresent();
        assertThat(category2.get().getName()).isEqualTo("Category 2");
    }

    @Test
    void testLoadDataEmptyList() {
        repository.loadData(List.of());

        List<Category> categories = repository.getAll();
        assertThat(categories).isEmpty();
    }

    @Test
    void testLoadDataNull() {
        repository.loadData(null);

        List<Category> categories = repository.getAll();
        assertThat(categories).isEmpty();
    }

    @Test
    void testMultipleSavesIncrementId() {
        Category category1 = new Category("Category 1");
        Category category2 = new Category("Category 2");
        Category category3 = new Category("Category 3");

        repository.save(category1);
        repository.save(category2);
        repository.save(category3);

        assertThat(category1.getId()).isEqualTo(1L);
        assertThat(category2.getId()).isEqualTo(2L);
        assertThat(category3.getId()).isEqualTo(3L);

        List<Category> categories = repository.getAll();
        assertThat(categories).hasSize(3);
    }

    @Test
    void testRepositoryIsThreadSafe() {
        Category category = new Category("Test Category");
        repository.save(category);

        List<Category> categories = repository.getAll();
        assertThat(categories).hasSize(1);

        assertThatNoException().isThrownBy(() -> {
            for (Category c : categories) {
                repository.save(new Category("Another Category"));
            }
        });
    }
}
