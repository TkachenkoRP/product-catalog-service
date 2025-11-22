package com.my.service.impl;

import com.my.exception.EntityNotFoundException;
import com.my.mapper.ProductMapper;
import com.my.model.AuditLog;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.ProductRepository;
import com.my.repository.impl.PostgresqlProductRepositoryImpl;
import com.my.service.AuditService;
import com.my.service.CacheService;
import com.my.service.ProductService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final CacheService cacheService;

    public ProductServiceImpl() {
        this(new PostgresqlProductRepositoryImpl(), new AuditServiceImpl(), new CacheService());
    }

    public ProductServiceImpl(ProductRepository productRepository, AuditService auditService, CacheService cacheService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
        this.cacheService = cacheService;
    }

    private final Map<String, Long> metrics = new HashMap<>();

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

    private List<Product> applyFilters(List<Product> products, ProductFilter filter) {
        return products.stream()
                .filter(product -> filterByCategory(product, filter.categoryId()))
                .filter(product -> filterByBrand(product, filter.brandId()))
                .filter(product -> filterByStock(product, filter.minStock()))
                .filter(product -> filterByPrice(product, filter.minPrice(), filter.maxPrice()))
                .toList();
    }

    private boolean filterByCategory(Product product, Long categoryId) {
        return categoryId == null || product.getCategoryId().equals(categoryId);
    }

    private boolean filterByBrand(Product product, Long brandId) {
        return brandId == null || product.getBrandId().equals(brandId);
    }

    private boolean filterByStock(Product product, Integer minStock) {
        return minStock == null || product.getStock() >= minStock;
    }

    private boolean filterByPrice(Product product, Double minPrice, Double maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return true;
        }
        if (minPrice == null) {
            return product.getPrice() <= maxPrice;
        }
        if (maxPrice == null) {
            return product.getPrice() >= minPrice;
        }
        return product.getPrice() >= minPrice && product.getPrice() <= maxPrice;
    }

    @Override
    public Product getById(Long id) {
        long startTime = System.nanoTime();

        Product product = (Product) cacheService.get(CacheService.CacheKey.PRODUCT + id.toString());

        if (product == null) {
            product = productRepository.getById(id).orElseThrow(
                    () -> new EntityNotFoundException(MessageFormat.format("Товар с id {0} не найден", id)));
            if (product != null) {
                cacheService.put(CacheService.CacheKey.PRODUCT + id.toString(), product);
            }
        }

        auditService.logAction(AuditLog.AuditActions.VIEW_PRODUCT, "Просмотрен товар ID: " + id);
        recordMetric("getProduct", System.nanoTime() - startTime);

        return product;
    }

    @Override
    public Product save(Product product) {
        long startTime = System.nanoTime();
        Product saved = productRepository.save(product);
        cacheService.invalidate(CacheService.CacheKey.ALL_PRODUCTS.name());
        auditService.logAction(AuditLog.AuditActions.ADD_PRODUCT, "Добавлен товар: " + product.getName());
        recordMetric("addProduct", System.nanoTime() - startTime);
        return saved;
    }

    @Override
    public Product update(Long id, Product sourceProduct) {
        long startTime = System.nanoTime();
        Product updatedProduct = getById(id);
        ProductMapper.INSTANCE.updateProduct(sourceProduct, updatedProduct);
        Product updated = productRepository.update(updatedProduct);
        cacheService.invalidate(CacheService.CacheKey.PRODUCT + id.toString());
        cacheService.invalidate(CacheService.CacheKey.ALL_PRODUCTS.name());
        auditService.logAction(AuditLog.AuditActions.UPDATE_PRODUCT, "Обновлен товар ID: " + id);
        recordMetric("updateProduct", System.nanoTime() - startTime);
        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        long startTime = System.nanoTime();
        boolean success = productRepository.deleteById(id);
        if (success) {
            auditService.logAction(AuditLog.AuditActions.DELETE_PRODUCT, "Удален товар ID: " + id);
            cacheService.invalidate(CacheService.CacheKey.PRODUCT + id.toString());
            cacheService.invalidate(CacheService.CacheKey.ALL_PRODUCTS.name());
        }
        recordMetric("deleteProduct", System.nanoTime() - startTime);
        return success;
    }

    private void recordMetric(String operation, long duration) {
        metrics.put(operation, duration);
    }

    @Override
    public Map<String, Long> getMetrics() {
        return metrics;
    }
}
