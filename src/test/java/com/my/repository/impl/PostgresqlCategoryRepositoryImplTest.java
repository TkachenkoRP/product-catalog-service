package com.my.repository.impl;

import com.my.model.Category;
import com.my.repository.AbstractPostgresqlRepositoryTest;
import com.my.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresqlCategoryRepositoryImplTest extends AbstractPostgresqlRepositoryTest {
    private final CategoryRepository categoryRepository;

    @Autowired
    PostgresqlCategoryRepositoryImplTest(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Test
    void whenGetAllCategories_thenReturnAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        assertThat(categories)
                .isNotNull()
                .hasSize(3)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Electronics", "Clothing", "Books");
    }

    @Test
    void whenGetCategoryById_withExistingId_thenReturnCategory() {
        List<Category> categories = categoryRepository.findAll();
        Long existingCategoryId = categories.get(0).getId();

        Optional<Category> categoryOpt = categoryRepository.findById(existingCategoryId);

        assertThat(categoryOpt).isPresent();
        categoryOpt.ifPresent(category -> {
            assertThat(category.getId()).isEqualTo(existingCategoryId);
            assertThat(category.getName()).isNotBlank();
        });
    }

    @Test
    void whenGetCategoryById_withNonExistingId_thenReturnEmpty() {
        Optional<Category> categoryOpt = categoryRepository.findById(999L);

        assertThat(categoryOpt).isEmpty();
    }

    @Test
    void whenSaveNewCategory_thenCategoryIsPersisted() {
        Category newCategory = new Category("Home Appliances");

        Category savedCategory = categoryRepository.save(newCategory);

        assertThat(savedCategory)
                .isNotNull()
                .extracting(Category::getId, Category::getName)
                .containsExactly(savedCategory.getId(), "Home Appliances");

        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());
        assertThat(foundCategory).isPresent();
    }

    @Test
    void whenUpdateExistingCategory_thenCategoryIsUpdated() {
        List<Category> categories = categoryRepository.findAll();
        Category existingCategory = categories.get(0);

        Category categoryToUpdate = new Category(existingCategory.getId(), "Updated Electronics");

        Category updatedCategory = categoryRepository.update(categoryToUpdate);

        assertThat(updatedCategory)
                .isNotNull()
                .extracting(Category::getName)
                .isEqualTo("Updated Electronics");

        Optional<Category> verifiedCategory = categoryRepository.findById(existingCategory.getId());
        assertThat(verifiedCategory)
                .isPresent()
                .get()
                .extracting(Category::getName)
                .isEqualTo("Updated Electronics");
    }

    @Test
    void whenUpdateCategory_withNonExistingId_thenThrowException() {
        Category nonExistentCategory = new Category(999L, "Non Existent");

        try {
            categoryRepository.update(nonExistentCategory);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Ошибка обновления категории с ID: 999");
        }
    }

    @Test
    void whenDeleteCategoryById_withExistingId_thenCategoryIsDeleted() {
        Category categoryToDelete = new Category("To Delete");
        Category savedCategory = categoryRepository.save(categoryToDelete);
        Long categoryId = savedCategory.getId();

        boolean deleted = categoryRepository.deleteById(categoryId);

        assertThat(deleted).isTrue();

        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        assertThat(foundCategory).isEmpty();
    }

    @Test
    void whenDeleteCategoryById_withNonExistingId_thenReturnFalse() {
        boolean deleted = categoryRepository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void whenCheckCategoryExistsByName_withExistingName_thenReturnTrue() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("electronics");

        assertThat(exists).isTrue();
    }

    @Test
    void whenCheckCategoryExistsByName_withNonExistingName_thenReturnFalse() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void whenSaveCategory_withDuplicateName_thenThrowException() {
        Category category1 = new Category("Unique Category");
        categoryRepository.save(category1);

        Category category2 = new Category("Unique Category");

        try {
            categoryRepository.save(category2);
        } catch (DataAccessException e) {
            assertThat(e).hasMessageContaining("category_name_key");
        }
    }
}
