package com.my.service.impl;

import com.my.annotation.Audition;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.ProductRepository;
import com.my.repository.impl.PostgresqlProductRepositoryImpl;
import com.my.service.CacheService;
import com.my.service.ProductService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Audition
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CacheService cacheService;

    public ProductServiceImpl() {
        this(new PostgresqlProductRepositoryImpl(), new CacheService());
    }

    public ProductServiceImpl(ProductRepository productRepository, CacheService cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }

    @Override
    public List<Product> getAll(ProductFilter filter) {
        return productRepository.getAll(filter);
    }

    private List<Product> getCachedProducts() {
        List<Product> products = (List<Product>) cacheService.get(CacheService.CacheKey.ALL_PRODUCTS.name());

        if (products == null) {
            products = productRepository.getAll(null);
            cacheService.put(CacheService.CacheKey.ALL_PRODUCTS.name(), new ArrayList<>(products));
        }

        return products;
    }

    @Override
    public Product getById(Long id) {
        Product product = (Product) cacheService.get(CacheService.CacheKey.PRODUCT + id.toString());

        if (product == null) {
            product = productRepository.getById(id).orElseThrow(
                    () -> new EntityNotFoundException(MessageFormat.format("Товар с id {0} не найден", id)));
            if (product != null) {
                cacheService.put(CacheService.CacheKey.PRODUCT + id.toString(), product);
            }
        }

        return product;
    }

    @Override
    public Product save(Product product) {
        Product saved = productRepository.save(product);
        cacheService.invalidate(CacheService.CacheKey.ALL_PRODUCTS.name());
        return saved;
    }

    @Override
    public Product update(Long id, Product sourceProduct) {
        Product updatedProduct = getById(id);
        ProductMapper.INSTANCE.updateProduct(sourceProduct, updatedProduct);
        Product updated = productRepository.update(updatedProduct);
        cacheService.invalidate(CacheService.CacheKey.PRODUCT + id.toString());
        cacheService.invalidate(CacheService.CacheKey.ALL_PRODUCTS.name());
        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        boolean success = productRepository.deleteById(id);
        if (success) {
            cacheService.invalidate(CacheService.CacheKey.PRODUCT + id.toString());
            cacheService.invalidate(CacheService.CacheKey.ALL_PRODUCTS.name());
        }
        return success;
    }
}
