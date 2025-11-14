package com.my.repository;

import com.my.model.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    List<Brand> getAll();

    Optional<Brand> getById(Long id);

    Brand save(Brand brand);

    Brand update(Brand brand);

    boolean deleteById(Long id);

    boolean existsByNameIgnoreCase(String brandName);
}
