package com.my.repository.impl;

import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.repository.BrandRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryBrandRepositoryImpl implements BrandRepository {
    private final Map<Long, Brand> repository = new ConcurrentHashMap<>();
    private final AtomicLong currentId = new AtomicLong(1);

    @Override
    public List<Brand> getAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public Optional<Brand> getById(Long id) {
        return Optional.ofNullable(repository.get(id))
                .map(BrandMapper.INSTANCE::copyBrand);
    }

    @Override
    public Brand save(Brand brand) {
        Long id = currentId.getAndIncrement();
        brand.setId(id);
        repository.put(id, BrandMapper.INSTANCE.copyBrand(brand));
        return brand;
    }

    @Override
    public Brand update(Brand brand) {
        Brand existing = repository.get(brand.getId());
        if (existing != null) {
            BrandMapper.INSTANCE.updateBrand(brand, existing);
        }
        return BrandMapper.INSTANCE.copyBrand(existing);
    }

    @Override
    public boolean deleteById(Long id) {
        if (getById(id).isPresent()) {
            repository.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByNameIgnoreCase(String brandName) {
        return repository.values().stream()
                .anyMatch(b -> b.getName().equalsIgnoreCase(brandName));
    }

    public void loadData(List<Brand> brands) {
        if (brands == null || brands.isEmpty()) {
            return;
        }

        long maxId = 0;

        for (Brand brand : brands) {
            repository.put(brand.getId(), brand);
            if (brand.getId() > maxId) {
                maxId = brand.getId();
            }
        }
        currentId.set(maxId + 1);

        System.out.println("Загружено брендов: " + brands.size() + ", установлен Id: " + currentId.get());
    }
}
