package com.my.repository.impl;

import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCategoryRepositoryImpl implements CategoryRepository {

    private final Map<Long, Category> repository = new ConcurrentHashMap<>();

    private final AtomicLong currentId = new AtomicLong(1);

    @Override
    public List<Category> getAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public Optional<Category> getById(Long id) {
        return Optional.ofNullable(repository.get(id))
                .map(CategoryMapper.INSTANCE::copyCategory);
    }

    @Override
    public Category save(Category category) {
        Long id = currentId.getAndIncrement();
        category.setId(id);
        repository.put(id, CategoryMapper.INSTANCE.copyCategory(category));
        return category;
    }

    @Override
    public Category update(Category category) {
        Category existing = repository.get(category.getId());
        if (existing != null) {
            CategoryMapper.INSTANCE.updateCategory(category, existing);
        }
        return CategoryMapper.INSTANCE.copyCategory(existing);
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
    public boolean existsByNameIgnoreCase(String categoryName) {
        return repository.values().stream().anyMatch(
                c -> c.getName().equalsIgnoreCase(categoryName)
        );
    }

    public void loadData(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }

        long maxId = 0;

        for (Category category : categories) {
            repository.put(category.getId(), category);
            if (category.getId() > maxId) {
                maxId = category.getId();
            }
        }
        currentId.set(maxId + 1);
        System.out.println("Загружено категорий: " + categories.size() + ", установлен Id: " + currentId.get());
    }
}
