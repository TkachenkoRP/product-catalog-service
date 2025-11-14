package com.my.repository.impl;

import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryProductRepositoryImpl implements ProductRepository {

    private final Map<Long, Product> repository = new ConcurrentHashMap<>();
    private final AtomicLong currentId = new AtomicLong(1);

    @Override
    public List<Product> getAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public Optional<Product> getById(Long id) {
        return Optional.ofNullable(repository.get(id));
    }

    @Override
    public Product save(Product product) {
        Long id = currentId.getAndIncrement();
        product.setId(id);
        repository.put(id, ProductMapper.INSTANCE.copyProduct(product));
        return product;
    }

    @Override
    public Product update(Product product) {
        Product existing = repository.get(product.getId());
        if (existing != null) {
            ProductMapper.INSTANCE.updateProduct(product, existing);
        }
        return ProductMapper.INSTANCE.copyProduct(existing);
    }

    @Override
    public boolean deleteById(Long id) {
        if (getById(id).isPresent()) {
            repository.remove(id);
            return true;
        }
        return false;
    }

    public void loadData(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        long maxId = 0;

        for (Product product : products) {
            repository.put(product.getId(), product);
            if (product.getId() > maxId) {
                maxId = product.getId();
            }
        }
        currentId.set(maxId + 1);
        System.out.println("Загружено товаров: " + products.size() + ", установлен Id: " + currentId.get());
    }
}
