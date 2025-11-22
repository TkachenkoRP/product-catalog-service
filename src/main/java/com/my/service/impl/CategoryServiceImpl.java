package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.CategoryRepository;
import com.my.repository.impl.PostgresqlCategoryRepositoryImpl;
import com.my.service.CacheService;
import com.my.service.CategoryService;
import com.my.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;
    private final ProductService productService;

    public CategoryServiceImpl() {
        this(new PostgresqlCategoryRepositoryImpl(), new ProductServiceImpl());
    }

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.cacheService = new CacheService();
    }

    @Override
    public List<Category> getAll() {
        List<Category> categories = (List<Category>) cacheService.get(CacheService.CacheKey.ALL_CATEGORIES.name());

        if (categories == null) {
            categories = categoryRepository.getAll();
            cacheService.put(CacheService.CacheKey.ALL_CATEGORIES.name(), new ArrayList<>(categories));
        }

        return categories;
    }

    @Override
    public Category getById(Long id) {
        Category category = (Category) cacheService.get(CacheService.CacheKey.CATEGORY + id.toString());
        if (category == null) {
            category = categoryRepository.getById(id).orElseThrow(
                    () -> new EntityNotFoundException(MessageFormat.format("Категория с id {0} не найдена", id)));
            if (category != null) {
                cacheService.put(CacheService.CacheKey.CATEGORY + id.toString(), category);
            }
        }
        return category;
    }

    @Override
    public Category save(Category category) {
        if (existsByName(category.getName())) {
            throw new AlreadyExistException(category.getName() + " уже существует");
        }
        Category saved = categoryRepository.save(category);
        cacheService.invalidate(CacheService.CacheKey.ALL_CATEGORIES.name());
        return saved;
    }

    @Override
    public Category update(Long id, Category sourceCategory) {
        Category updatedCategory = getById(id);
        if (existsByName(sourceCategory.getName())) {
            throw new AlreadyExistException(sourceCategory.getName() + " уже существует");
        }
        CategoryMapper.INSTANCE.updateCategory(sourceCategory, updatedCategory);
        Category updated = categoryRepository.update(updatedCategory);
        cacheService.invalidate(CacheService.CacheKey.CATEGORY + id.toString());
        cacheService.invalidate(CacheService.CacheKey.ALL_CATEGORIES.name());
        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        if (hasProductsWithCategory(id)) {
            return false;
        }
        boolean success = categoryRepository.deleteById(id);
        if (success) {
            cacheService.invalidate(CacheService.CacheKey.CATEGORY + id.toString());
            cacheService.invalidate(CacheService.CacheKey.ALL_CATEGORIES.name());
        }
        return success;
    }

    private boolean hasProductsWithCategory(Long categoryId) {
        ProductFilter filter = new ProductFilter(categoryId, null, null, null, null);
        List<Product> products = productService.getAll(filter);
        return !products.isEmpty();
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }
}
