package com.my.app;

import com.my.repository.BrandRepository;
import com.my.repository.CategoryRepository;
import com.my.repository.ProductRepository;
import com.my.repository.UserRepository;
import com.my.service.AuditService;
import com.my.service.BrandService;
import com.my.service.CacheService;
import com.my.service.CategoryService;
import com.my.service.ProductService;
import com.my.service.UserService;

public interface ServiceCreator {
    UserService createUserService(UserRepository userRepository);

    CategoryService createCategoryService(CategoryRepository categoryRepository);

    BrandService createBrandService(BrandRepository brandRepository);

    ProductService createProductService(ProductRepository productRepository);

    AuditService createAuditService();

    CacheService createCacheService();
}
