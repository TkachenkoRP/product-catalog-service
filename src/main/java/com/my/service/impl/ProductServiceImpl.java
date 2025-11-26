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
import com.my.util.CacheKeyGenerator;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Audition
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CacheService cacheService;

    public ProductServiceImpl() {
        this(new PostgresqlProductRepositoryImpl(), new RedisCacheServiceImpl());
    }

    @Override
    public List<Product> getAll(ProductFilter filter) {
        if (filter != null && hasFilters(filter)) {
            return productRepository.getAll(filter);
        }

        String cacheKey = CacheKeyGenerator.generateAllProductsKey();

        List<Product> products = cacheService.getList(cacheKey, Product.class);
        System.out.println("Кэш:\n" + products);

        if (products == null) {
            products = productRepository.getAll(filter);
            cacheService.put(cacheKey, products);
        }

        return products;
    }

    @Override
    public Product getById(Long id) {
        String cacheKey = CacheKeyGenerator.generateProductKey(id);
        Product product = cacheService.get(cacheKey, Product.class);
        System.out.println("Кэш:\n" + product);

        if (product == null) {
            product = productRepository.getById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Товар с id " + id + " не найден"));
            cacheService.put(cacheKey, product);
        }

        return product;
    }

    @Override
    public Product save(Product product) {
        Product saved = productRepository.save(product);
        cacheService.invalidate(CacheKeyGenerator.generateAllProductsKey());
        return saved;
    }

    @Override
    public Product update(Long id, Product sourceProduct) {
        Product updatedProduct = getById(id);
        ProductMapper.INSTANCE.updateProduct(sourceProduct, updatedProduct);
        Product updated = productRepository.update(updatedProduct);

        cacheService.invalidate(CacheKeyGenerator.generateProductKey(id));
        cacheService.invalidate(CacheKeyGenerator.generateAllProductsKey());

        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        boolean success = productRepository.deleteById(id);
        if (success) {
            cacheService.invalidate(CacheKeyGenerator.generateProductKey(id));
            cacheService.invalidate(CacheKeyGenerator.generateAllProductsKey());
        }
        return success;
    }

    private boolean hasFilters(ProductFilter filter) {
        return filter.categoryId() != null ||
               filter.brandId() != null ||
               filter.minPrice() != null ||
               filter.maxPrice() != null ||
               filter.minStock() != null;
    }
}
