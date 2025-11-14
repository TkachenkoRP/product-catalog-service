package com.my.repository;

import com.my.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> getAll();

    Optional<Product> getById(Long id);

    Product save(Product product);

    Product update(Product product);

    boolean deleteById(Long id);
}
