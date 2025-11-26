package com.my.service.impl;

import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.CategoryRepository;
import com.my.repository.impl.PostgresqlCategoryRepositoryImpl;
import com.my.service.CategoryService;
import com.my.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.util.List;

@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public CategoryServiceImpl() {
        this(new PostgresqlCategoryRepositoryImpl(), new ProductServiceImpl());
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.getAll();
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.getById(id).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format("Категория с id {0} не найдена", id)));
    }

    @Override
    public Category save(Category category) {
        if (existsByName(category.getName())) {
            throw new AlreadyExistException(category.getName() + " уже существует");
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, Category sourceCategory) {
        Category updatedCategory = getById(id);
        if (existsByName(sourceCategory.getName())) {
            throw new AlreadyExistException(sourceCategory.getName() + " уже существует");
        }
        CategoryMapper.INSTANCE.updateCategory(sourceCategory, updatedCategory);
        return categoryRepository.update(updatedCategory);
    }

    @Override
    public boolean deleteById(Long id) {
        if (hasProductsWithCategory(id)) {
            return false;
        }
        return categoryRepository.deleteById(id);
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
