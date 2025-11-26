package com.my.service.impl;

import com.my.annotation.Audition;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.ProductRepository;
import com.my.repository.impl.PostgresqlProductRepositoryImpl;
import com.my.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Audition
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl() {
        this(new PostgresqlProductRepositoryImpl());
    }

    @Override
    public List<Product> getAll(ProductFilter filter) {
        return productRepository.getAll(filter);
    }

    @Override
    public Product getById(Long id) {
        return productRepository.getById(id).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format("Товар с id {0} не найден", id)));
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product sourceProduct) {
        Product updatedProduct = getById(id);
        ProductMapper.INSTANCE.updateProduct(sourceProduct, updatedProduct);
        return productRepository.update(updatedProduct);
    }

    @Override
    public boolean deleteById(Long id) {
        return productRepository.deleteById(id);
    }
}
