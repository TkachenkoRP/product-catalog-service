package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityHasReferencesException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.repository.BrandRepository;
import com.my.service.BrandService;
import com.my.service.CacheService;
import com.my.service.CatalogValidationService;
import com.my.util.CacheKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final CatalogValidationService validationService;
    private final CacheService cacheService;
    private final BrandMapper brandMapper;

    @Override
    public List<Brand> getAll() {
        String cacheKey = CacheKeyGenerator.generateAllBrandsKey();
        List<Brand> brands = cacheService.getList(cacheKey, Brand.class);

        if (brands == null) {
            brands = brandRepository.getAll();
            cacheService.put(cacheKey, brands);
        }

        return brands;
    }

    @Override
    public Brand getById(Long id) {
        String cacheKey = CacheKeyGenerator.generateBrandKey(id);
        Brand brand = cacheService.get(cacheKey, Brand.class);

        if (brand == null) {
            brand = brandRepository.getById(id).orElseThrow(
                    () -> new EntityNotFoundException(MessageFormat.format("Бренд с id {0} не найден", id)));
            cacheService.put(cacheKey, brand);
        }

        return brand;
    }

    @Override
    public Brand save(Brand brand) {
        if (existsByName(brand.getName())) {
            throw new AlreadyExistException(brand.getName() + " уже существует");
        }
        Brand saved = brandRepository.save(brand);
        cacheService.invalidate(CacheKeyGenerator.generateAllBrandsKey());
        return saved;
    }

    @Override
    public Brand update(Long id, Brand sourceBrand) {
        Brand updatedBrand = getById(id);
        if (existsByName(sourceBrand.getName())) {
            throw new AlreadyExistException(sourceBrand.getName() + " уже существует");
        }
        brandMapper.updateBrand(sourceBrand, updatedBrand);
        Brand updated = brandRepository.update(updatedBrand);

        cacheService.invalidate(CacheKeyGenerator.generateBrandKey(id));
        cacheService.invalidate(CacheKeyGenerator.generateAllBrandsKey());

        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        if (validationService.brandHasProducts(id)) {
            throw new EntityHasReferencesException("бренд", id);
        }
        boolean success = brandRepository.deleteById(id);
        if (success) {
            cacheService.invalidate(CacheKeyGenerator.generateBrandKey(id));
            cacheService.invalidate(CacheKeyGenerator.generateAllBrandsKey());
        }
        return success;
    }

    @Override
    public boolean existsByName(String name) {
        return brandRepository.existsByNameIgnoreCase(name);
    }
}
