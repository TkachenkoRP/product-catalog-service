package com.my.service;

import com.my.model.Brand;

import java.util.List;

public interface BrandService {
    List<Brand> getAll();

    Brand getById(Long id);

    Brand save(Brand brand);

    Brand update(Long id, Brand brand);

    boolean deleteById(Long id);

    boolean existsByName(String name);
}
