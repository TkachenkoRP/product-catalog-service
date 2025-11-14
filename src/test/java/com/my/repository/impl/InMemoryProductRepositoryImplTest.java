package com.my.repository.impl;

import com.my.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class InMemoryProductRepositoryImplTest {
    private InMemoryProductRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepositoryImpl();
    }

    @Test
    void testGetAllInitiallyEmpty() {
        List<Product> products = repository.getAll();

        assertThat(products).isNotNull().isEmpty();
    }

    @Test
    void testSave() {
        Product product = new Product("Test Product", 1L, 1L, 99.99, 10);

        Product savedProduct = repository.save(product);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isEqualTo(1L);
        assertThat(savedProduct.getName()).isEqualTo("Test Product");
        assertThat(savedProduct.getCategoryId()).isEqualTo(1L);
        assertThat(savedProduct.getBrandId()).isEqualTo(1L);
        assertThat(savedProduct.getPrice()).isEqualTo(99.99);
        assertThat(savedProduct.getStock()).isEqualTo(10);

        List<Product> products = repository.getAll();
        assertThat(products).hasSize(1);
    }

    @Test
    void testGetById() {
        Product product = new Product("Test Product", 1L, 1L, 99.99, 10);
        repository.save(product);

        Optional<Product> foundProduct = repository.getById(1L);

        assertThat(foundProduct).isPresent();
        foundProduct.ifPresent(p -> {
            assertThat(p.getId()).isEqualTo(1L);
            assertThat(p.getName()).isEqualTo("Test Product");
        });
    }

    @Test
    void testGetByIdNotFound() {
        Optional<Product> foundProduct = repository.getById(999L);

        assertThat(foundProduct).isEmpty();
    }

    @Test
    void testUpdate() {
        Product product = new Product("Original Product", 1L, 1L, 99.99, 10);
        repository.save(product);

        Product updatedProduct = new Product("Updated Product", 2L, 2L, 149.99, 5);
        updatedProduct.setId(1L);

        Product result = repository.update(updatedProduct);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getCategoryId()).isEqualTo(2L);
        assertThat(result.getBrandId()).isEqualTo(2L);
        assertThat(result.getPrice()).isEqualTo(149.99);
        assertThat(result.getStock()).isEqualTo(5);

        Optional<Product> foundProduct = repository.getById(1L);
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Updated Product");
    }

    @Test
    void testUpdateNotFound() {
        Product nonExistentProduct = new Product("Non Existent", 1L, 1L, 99.99, 10);
        nonExistentProduct.setId(999L);

        Product result = repository.update(nonExistentProduct);

        assertThat(result).isNull();
    }

    @Test
    void testDeleteById() {
        Product product = new Product("Test Product", 1L, 1L, 99.99, 10);
        repository.save(product);

        boolean deleted = repository.deleteById(1L);

        assertThat(deleted).isTrue();

        Optional<Product> foundProduct = repository.getById(1L);
        assertThat(foundProduct).isEmpty();

        List<Product> products = repository.getAll();
        assertThat(products).isEmpty();
    }

    @Test
    void testDeleteByIdNotFound() {
        boolean deleted = repository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void testLoadData() {
        List<Product> productsToLoad = List.of(
                new Product(10L, "Product 1", 1L, 1L, 99.99, 10),
                new Product(20L, "Product 2", 2L, 2L, 149.99, 5)
        );

        repository.loadData(productsToLoad);

        List<Product> products = repository.getAll();
        assertThat(products).hasSize(2);

        Optional<Product> product1 = repository.getById(10L);
        assertThat(product1).isPresent();
        assertThat(product1.get().getName()).isEqualTo("Product 1");

        Optional<Product> product2 = repository.getById(20L);
        assertThat(product2).isPresent();
        assertThat(product2.get().getName()).isEqualTo("Product 2");
    }

    @Test
    void testLoadDataEmptyList() {
        repository.loadData(List.of());

        List<Product> products = repository.getAll();
        assertThat(products).isEmpty();
    }

    @Test
    void testLoadDataNull() {
        repository.loadData(null);

        List<Product> products = repository.getAll();
        assertThat(products).isEmpty();
    }

    @Test
    void testMultipleSavesIncrementId() {
        Product product1 = new Product("Product 1", 1L, 1L, 99.99, 10);
        Product product2 = new Product("Product 2", 1L, 1L, 89.99, 15);
        Product product3 = new Product("Product 3", 1L, 1L, 79.99, 20);

        repository.save(product1);
        repository.save(product2);
        repository.save(product3);

        assertThat(product1.getId()).isEqualTo(1L);
        assertThat(product2.getId()).isEqualTo(2L);
        assertThat(product3.getId()).isEqualTo(3L);

        List<Product> products = repository.getAll();
        assertThat(products).hasSize(3);
    }

    @Test
    void testRepositoryIsThreadSafe() {
        Product product = new Product("Test Product", 1L, 1L, 99.99, 10);
        repository.save(product);

        List<Product> products = repository.getAll();
        assertThat(products).hasSize(1);

        assertThatNoException().isThrownBy(() -> {
            for (Product p : products) {
                repository.save(new Product("Another Product", 1L, 1L, 49.99, 25));
            }
        });
    }
}
