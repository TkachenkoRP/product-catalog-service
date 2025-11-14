package com.my.service.impl;

import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.BrandRepository;
import com.my.service.BrandService;
import com.my.service.CacheService;
import com.my.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final CacheService cacheService;
    private final ProductService productService;

    @Override
    public List<Brand> getAll() {
        List<Brand> brands = (List<Brand>) cacheService.get(CacheService.CacheKey.ALL_BRANDS.name());

        if (brands == null) {
            brands = brandRepository.getAll();
            cacheService.put(CacheService.CacheKey.ALL_BRANDS.name(), new ArrayList<>(brands));
        }

        return brands;
    }

    @Override
    public Brand getById(Long id) {
        Brand brand = (Brand) cacheService.get(CacheService.CacheKey.BRAND + id.toString());
        if (brand == null) {
            brand = brandRepository.getById(id).orElse(null);
            if (brand != null) {
                cacheService.put(CacheService.CacheKey.BRAND + id.toString(), brand);
            }
        }
        return brand;
    }

    @Override
    public Brand save(Brand brand) {
        if (existsByName(brand.getName())) {
            return null;
        }
        Brand saved = brandRepository.save(brand);
        cacheService.invalidate(CacheService.CacheKey.ALL_BRANDS.name());
        return saved;
    }

    @Override
    public Brand update(Long id, Brand sourceBrand) {
        Brand updatedBrand = getById(id);
        if (updatedBrand == null || existsByName(sourceBrand.getName())) {
            return null;
        }
        BrandMapper.INSTANCE.updateBrand(sourceBrand, updatedBrand);
        Brand updated = brandRepository.update(updatedBrand);
        cacheService.invalidate(CacheService.CacheKey.BRAND + id.toString());
        cacheService.invalidate(CacheService.CacheKey.ALL_BRANDS.name());
        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        if (hasProductsWithBrand(id)) {
            return false;
        }
        boolean success = brandRepository.deleteById(id);
        if (success) {
            cacheService.invalidate(CacheService.CacheKey.BRAND + id.toString());
            cacheService.invalidate(CacheService.CacheKey.ALL_BRANDS.name());
        }
        return success;
    }

    private boolean hasProductsWithBrand(Long brandId) {
        ProductFilter filter = new ProductFilter(null, brandId, null, null, null);
        List<Product> products = productService.getAll(filter);
        return !products.isEmpty();
    }

    @Override
    public boolean existsByName(String name) {
        return brandRepository.existsByNameIgnoreCase(name);
    }
}
