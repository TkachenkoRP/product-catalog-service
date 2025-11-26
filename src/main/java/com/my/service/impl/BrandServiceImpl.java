package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.BrandRepository;
import com.my.repository.impl.PostgresqlBrandRepositoryImpl;
import com.my.service.BrandService;
import com.my.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.util.List;

@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductService productService;

    public BrandServiceImpl() {
        this(new PostgresqlBrandRepositoryImpl(), new ProductServiceImpl());
    }

    @Override
    public List<Brand> getAll() {
        return brandRepository.getAll();
    }

    @Override
    public Brand getById(Long id) {
        return brandRepository.getById(id).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format("Бренд с id {0} не найден", id)));
    }

    @Override
    public Brand save(Brand brand) {
        if (existsByName(brand.getName())) {
            throw new AlreadyExistException(brand.getName() + " уже существует");
        }
        return brandRepository.save(brand);
    }

    @Override
    public Brand update(Long id, Brand sourceBrand) {
        Brand updatedBrand = getById(id);
        if (existsByName(sourceBrand.getName())) {
            throw new AlreadyExistException(sourceBrand.getName() + " уже существует");
        }
        BrandMapper.INSTANCE.updateBrand(sourceBrand, updatedBrand);
        return brandRepository.update(updatedBrand);
    }

    @Override
    public boolean deleteById(Long id) {
        if (hasProductsWithBrand(id)) {
            return false;
        }
        return brandRepository.deleteById(id);
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
