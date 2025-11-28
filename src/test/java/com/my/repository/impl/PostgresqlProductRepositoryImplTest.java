package com.my.repository.impl;

import com.my.exception.ProductCreationException;
import com.my.model.Brand;
import com.my.model.Category;
import com.my.model.Product;
import com.my.repository.AbstractPostgresqlRepositoryTest;
import com.my.repository.BrandRepository;
import com.my.repository.CategoryRepository;
import com.my.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresqlProductRepositoryImplTest extends AbstractPostgresqlRepositoryTest {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Autowired
    PostgresqlProductRepositoryImplTest(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @Test
    void whenGetAllProducts_thenReturnAllProducts() {
        List<Product> products = productRepository.getAll(null);

        assertThat(products)
                .isNotNull()
                .hasSize(18)
                .allSatisfy(product -> {
                    assertThat(product.getId()).isNotNull();
                    assertThat(product.getName()).isNotBlank();
                    assertThat(product.getCategoryId()).isNotNull();
                    assertThat(product.getBrandId()).isNotNull();
                    assertThat(product.getPrice()).isPositive();
                    assertThat(product.getStock()).isNotNull();
                });
    }

    @Test
    void whenGetProductById_withExistingId_thenReturnProduct() {
        List<Product> products = productRepository.getAll(null);
        Long existingProductId = products.get(0).getId();

        Optional<Product> productOpt = productRepository.getById(existingProductId);

        assertThat(productOpt).isPresent();
        productOpt.ifPresent(product -> {
            assertThat(product.getId()).isEqualTo(existingProductId);
            assertThat(product.getName()).isNotBlank();
            assertThat(product.getCategoryId()).isNotNull();
            assertThat(product.getBrandId()).isNotNull();
            assertThat(product.getPrice()).isPositive();
            assertThat(product.getStock()).isNotNull();
        });
    }

    @Test
    void whenGetProductById_withNonExistingId_thenReturnEmpty() {
        Optional<Product> productOpt = productRepository.getById(999L);

        assertThat(productOpt).isEmpty();
    }

    @Test
    void whenSaveNewProduct_thenProductIsPersisted() {
        List<Category> categories = categoryRepository.getAll();
        List<Brand> brands = brandRepository.getAll();

        Product newProduct = new Product(
                "New Test Product",
                categories.get(0).getId(),
                brands.get(0).getId(),
                199.99,
                25
        );

        Product savedProduct = productRepository.save(newProduct);

        assertThat(savedProduct)
                .isNotNull()
                .extracting(
                        Product::getName,
                        Product::getCategoryId,
                        Product::getBrandId,
                        Product::getPrice,
                        Product::getStock
                )
                .containsExactly(
                        "New Test Product",
                        categories.get(0).getId(),
                        brands.get(0).getId(),
                        199.99,
                        25
                );

        Optional<Product> foundProduct = productRepository.getById(savedProduct.getId());
        assertThat(foundProduct).isPresent();
    }

    @Test
    void whenUpdateExistingProduct_thenProductIsUpdated() {
        List<Product> products = productRepository.getAll(null);
        Product existingProduct = products.get(0);

        List<Category> categories = categoryRepository.getAll();
        List<Brand> brands = brandRepository.getAll();

        Product productToUpdate = new Product(
                existingProduct.getId(),
                "Updated Product",
                categories.get(1).getId(),
                brands.get(1).getId(),
                299.99,
                10
        );

        Product updatedProduct = productRepository.update(productToUpdate);

        assertThat(updatedProduct)
                .isNotNull()
                .extracting(
                        Product::getName,
                        Product::getCategoryId,
                        Product::getBrandId,
                        Product::getPrice,
                        Product::getStock
                )
                .containsExactly(
                        "Updated Product",
                        categories.get(1).getId(),
                        brands.get(1).getId(),
                        299.99,
                        10
                );

        Optional<Product> verifiedProduct = productRepository.getById(existingProduct.getId());
        assertThat(verifiedProduct)
                .isPresent()
                .get()
                .extracting(Product::getName)
                .isEqualTo("Updated Product");
    }

    @Test
    void whenUpdateProduct_withNonExistingId_thenThrowException() {
        Product nonExistentProduct = new Product(999L, "Non Existent", 1L, 1L, 99.99, 10);

        try {
            productRepository.update(nonExistentProduct);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Ошибка поиска товара с ID: 999");
        }
    }

    @Test
    void whenDeleteProductById_withExistingId_thenProductIsDeleted() {
        List<Category> categories = categoryRepository.getAll();
        List<Brand> brands = brandRepository.getAll();

        Product productToDelete = new Product(
                "Product To Delete",
                categories.get(0).getId(),
                brands.get(0).getId(),
                99.99,
                5
        );
        Product savedProduct = productRepository.save(productToDelete);
        Long productId = savedProduct.getId();

        boolean deleted = productRepository.deleteById(productId);

        assertThat(deleted).isTrue();

        Optional<Product> foundProduct = productRepository.getById(productId);
        assertThat(foundProduct).isEmpty();
    }

    @Test
    void whenDeleteProductById_withNonExistingId_thenReturnFalse() {
        boolean deleted = productRepository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void whenSaveProduct_withInvalidForeignKey_thenThrowException() {
        Product invalidProduct = new Product("Invalid Product", 999L, 999L, 99.99, 10);

        try {
            productRepository.save(invalidProduct);
        } catch (ProductCreationException e) {
            assertThat(e).hasMessageContaining("Ошибка добавления товара");
        }
    }

    @Test
    void whenSaveMultipleProducts_withSameName_thenAllAreSaved() {
        List<Category> categories = categoryRepository.getAll();
        List<Brand> brands = brandRepository.getAll();

        Product product1 = new Product("Same Name Product", categories.get(0).getId(), brands.get(0).getId(), 99.99, 10);
        Product product2 = new Product("Same Name Product", categories.get(1).getId(), brands.get(1).getId(), 149.99, 5);

        Product saved1 = productRepository.save(product1);
        Product saved2 = productRepository.save(product2);

        assertThat(saved1).isNotNull();
        assertThat(saved2).isNotNull();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());

        List<Product> allProducts = productRepository.getAll(null);
        long sameNameCount = allProducts.stream()
                .filter(p -> p.getName().equals("Same Name Product"))
                .count();
        assertThat(sameNameCount).isEqualTo(2);
    }
}
