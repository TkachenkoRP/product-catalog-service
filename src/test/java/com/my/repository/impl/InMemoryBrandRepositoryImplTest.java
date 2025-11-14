package com.my.repository.impl;

import com.my.model.Brand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryBrandRepositoryImplTest {
    private InMemoryBrandRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBrandRepositoryImpl();
    }

    @Test
    void testGetAllInitiallyEmpty() {
        List<Brand> brands = repository.getAll();

        assertThat(brands).isNotNull().isEmpty();
    }

    @Test
    void testSave() {
        Brand brand = new Brand("Test Brand");

        Brand savedBrand = repository.save(brand);

        assertThat(savedBrand).isNotNull();
        assertThat(savedBrand.getId()).isEqualTo(1L);
        assertThat(savedBrand.getName()).isEqualTo("Test Brand");

        List<Brand> brands = repository.getAll();
        assertThat(brands).hasSize(1);
    }

    @Test
    void testGetById() {
        Brand brand = new Brand("Test Brand");
        repository.save(brand);

        Optional<Brand> foundBrand = repository.getById(1L);

        assertThat(foundBrand).isPresent();
        foundBrand.ifPresent(b -> {
            assertThat(b.getId()).isEqualTo(1L);
            assertThat(b.getName()).isEqualTo("Test Brand");
        });
    }

    @Test
    void testGetByIdNotFound() {
        Optional<Brand> foundBrand = repository.getById(999L);

        assertThat(foundBrand).isEmpty();
    }

    @Test
    void testUpdate() {
        Brand brand = new Brand("Original Brand");
        repository.save(brand);

        Brand updatedBrand = new Brand("Updated Brand");
        updatedBrand.setId(1L);

        Brand result = repository.update(updatedBrand);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Brand");

        Optional<Brand> foundBrand = repository.getById(1L);
        assertThat(foundBrand).isPresent();
        assertThat(foundBrand.get().getName()).isEqualTo("Updated Brand");
    }

    @Test
    void testUpdateNotFound() {
        Brand nonExistentBrand = new Brand("Non Existent");
        nonExistentBrand.setId(999L);

        Brand result = repository.update(nonExistentBrand);

        assertThat(result).isNull();
    }

    @Test
    void testDeleteById() {
        Brand brand = new Brand("Test Brand");
        repository.save(brand);

        boolean deleted = repository.deleteById(1L);

        assertThat(deleted).isTrue();

        Optional<Brand> foundBrand = repository.getById(1L);
        assertThat(foundBrand).isEmpty();

        List<Brand> brands = repository.getAll();
        assertThat(brands).isEmpty();
    }

    @Test
    void testDeleteByIdNotFound() {
        boolean deleted = repository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void testExistsByNameIgnoreCase() {
        Brand brand = new Brand("Test Brand");
        repository.save(brand);

        boolean exists = repository.existsByNameIgnoreCase("test brand");
        assertThat(exists).isTrue();

        boolean existsExact = repository.existsByNameIgnoreCase("Test Brand");
        assertThat(existsExact).isTrue();

        boolean notExists = repository.existsByNameIgnoreCase("Nonexistent Brand");
        assertThat(notExists).isFalse();
    }

    @Test
    void testLoadData() {
        List<Brand> brandsToLoad = List.of(
                new Brand(10L, "Brand 1"),
                new Brand(20L, "Brand 2")
        );

        repository.loadData(brandsToLoad);

        List<Brand> brands = repository.getAll();
        assertThat(brands).hasSize(2);

        Optional<Brand> brand1 = repository.getById(10L);
        assertThat(brand1).isPresent();
        assertThat(brand1.get().getName()).isEqualTo("Brand 1");

        Optional<Brand> brand2 = repository.getById(20L);
        assertThat(brand2).isPresent();
        assertThat(brand2.get().getName()).isEqualTo("Brand 2");
    }

    @Test
    void testLoadDataEmptyList() {
        repository.loadData(List.of());

        List<Brand> brands = repository.getAll();
        assertThat(brands).isEmpty();
    }

    @Test
    void testLoadDataNull() {
        repository.loadData(null);

        List<Brand> brands = repository.getAll();
        assertThat(brands).isEmpty();
    }

    @Test
    void testMultipleSavesIncrementId() {
        Brand brand1 = new Brand("Brand 1");
        Brand brand2 = new Brand("Brand 2");
        Brand brand3 = new Brand("Brand 3");

        repository.save(brand1);
        repository.save(brand2);
        repository.save(brand3);

        assertThat(brand1.getId()).isEqualTo(1L);
        assertThat(brand2.getId()).isEqualTo(2L);
        assertThat(brand3.getId()).isEqualTo(3L);

        List<Brand> brands = repository.getAll();
        assertThat(brands).hasSize(3);
    }
}
