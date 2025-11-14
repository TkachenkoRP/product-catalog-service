package com.my.service;

import com.my.model.Product;
import com.my.model.ProductFilter;

import java.util.List;
import java.util.Map;

public interface ProductService {
    List<Product> getAll(ProductFilter filter);

    Product getById(Long id);

    Product save(Product product);

    Product update(Long id, Product product);

    boolean deleteById(Long id);

    Map<String, Long> getMetrics();
}
