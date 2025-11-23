package com.my.repository.impl;

import com.my.model.Brand;
import com.my.repository.AbstractPostgresqlRepositoryTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresqlBrandRepositoryImplTest extends AbstractPostgresqlRepositoryTest {
    @Test
    void whenGetAllBrands_thenReturnAllBrands() {
        List<Brand> brands = brandRepository.getAll();

        assertThat(brands)
                .isNotNull()
                .hasSize(3)
                .extracting(Brand::getName)
                .containsExactlyInAnyOrder("Samsung", "Nike", "Apple");
    }

    @Test
    void whenGetBrandById_withExistingId_thenReturnBrand() {
        List<Brand> brands = brandRepository.getAll();
        Long existingBrandId = brands.get(0).getId();

        Optional<Brand> brandOpt = brandRepository.getById(existingBrandId);

        assertThat(brandOpt).isPresent();
        brandOpt.ifPresent(brand -> {
            assertThat(brand.getId()).isEqualTo(existingBrandId);
            assertThat(brand.getName()).isNotBlank();
        });
    }

    @Test
    void whenGetBrandById_withNonExistingId_thenReturnEmpty() {
        Optional<Brand> brandOpt = brandRepository.getById(999L);

        assertThat(brandOpt).isEmpty();
    }

    @Test
    void whenSaveNewBrand_thenBrandIsPersisted() {
        Brand newBrand = new Brand("Sony");

        Brand savedBrand = brandRepository.save(newBrand);

        assertThat(savedBrand)
                .isNotNull()
                .extracting(Brand::getId, Brand::getName)
                .containsExactly(savedBrand.getId(), "Sony");

        Optional<Brand> foundBrand = brandRepository.getById(savedBrand.getId());
        assertThat(foundBrand).isPresent();
    }

    @Test
    void whenUpdateExistingBrand_thenBrandIsUpdated() {
        List<Brand> brands = brandRepository.getAll();
        Brand existingBrand = brands.get(0);

        Brand brandToUpdate = new Brand(existingBrand.getId(), "Updated Samsung");

        Brand updatedBrand = brandRepository.update(brandToUpdate);

        assertThat(updatedBrand)
                .isNotNull()
                .extracting(Brand::getName)
                .isEqualTo("Updated Samsung");

        Optional<Brand> verifiedBrand = brandRepository.getById(existingBrand.getId());
        assertThat(verifiedBrand)
                .isPresent()
                .get()
                .extracting(Brand::getName)
                .isEqualTo("Updated Samsung");
    }

    @Test
    void whenUpdateBrand_withNonExistingId_thenThrowException() {
        Brand nonExistentBrand = new Brand(999L, "Non Existent");

        try {
            brandRepository.update(nonExistentBrand);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Ошибка обновления бренда с ID: 999");
        }
    }

    @Test
    void whenDeleteBrandById_withExistingId_thenBrandIsDeleted() {
        Brand brandToDelete = new Brand("To Delete");
        Brand savedBrand = brandRepository.save(brandToDelete);
        Long brandId = savedBrand.getId();

        boolean deleted = brandRepository.deleteById(brandId);

        assertThat(deleted).isTrue();

        Optional<Brand> foundBrand = brandRepository.getById(brandId);
        assertThat(foundBrand).isEmpty();
    }

    @Test
    void whenDeleteBrandById_withNonExistingId_thenReturnFalse() {
        boolean deleted = brandRepository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void whenCheckBrandExistsByName_withExistingName_thenReturnTrue() {
        boolean exists = brandRepository.existsByNameIgnoreCase("samsung");

        assertThat(exists).isTrue();
    }

    @Test
    void whenCheckBrandExistsByName_withNonExistingName_thenReturnFalse() {
        boolean exists = brandRepository.existsByNameIgnoreCase("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void whenSaveBrand_withDuplicateName_thenThrowException() {
        Brand brand1 = new Brand("Unique Brand");
        brandRepository.save(brand1);

        Brand brand2 = new Brand("Unique Brand");

        try {
            brandRepository.save(brand2);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Ошибка добавления бренда");
        }
    }
}
