package com.my.app;

import com.my.repository.BrandRepository;
import com.my.repository.CategoryRepository;
import com.my.repository.ProductRepository;
import com.my.repository.UserRepository;
import com.my.repository.impl.InMemoryBrandRepositoryImpl;
import com.my.repository.impl.InMemoryCategoryRepositoryImpl;
import com.my.repository.impl.InMemoryProductRepositoryImpl;
import com.my.repository.impl.InMemoryUserRepositoryImpl;
import com.my.service.AuditService;
import com.my.service.BrandService;
import com.my.service.CacheService;
import com.my.service.CategoryService;
import com.my.service.CsvDataService;
import com.my.service.ProductService;
import com.my.service.UserService;
import com.my.service.impl.AuditServiceImpl;
import com.my.service.impl.BrandServiceImpl;
import com.my.service.impl.CategoryServiceImpl;
import com.my.service.impl.ProductServiceImpl;
import com.my.service.impl.UserServiceImpl;

/**
 * Конструктор фабрики in-memory сервисов.
 */
public class InMemoryServiceFactory implements ServiceFactory, ServiceCreator {

    private final UserService userService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductService productService;
    private final AuditService auditService;
    private final CacheService cacheService;
    private final CsvDataService csvDataService;

    public InMemoryServiceFactory() {
        UserRepository userRepository = new InMemoryUserRepositoryImpl();
        CategoryRepository categoryRepository = new InMemoryCategoryRepositoryImpl();
        BrandRepository brandRepository = new InMemoryBrandRepositoryImpl();
        ProductRepository productRepository = new InMemoryProductRepositoryImpl();

        this.cacheService = createCacheService();
        this.auditService = createAuditService();
        this.userService = createUserService(userRepository);
        this.productService = createProductService(productRepository);
        this.categoryService = createCategoryService(categoryRepository);
        this.brandService = createBrandService(brandRepository);
        this.csvDataService = new CsvDataService(userRepository, categoryRepository, brandRepository, productRepository);

        loadData();
    }

    private void loadData() {
        if (csvDataService.hasSavedData()) {
            System.out.println("Обнаружены сохраненные данные...");
            csvDataService.loadAllData();
        } else {
            System.out.println("Файлы данных не найдены, используются начальные данные");
        }
    }

    @Override
    public UserService createUserService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    @Override
    public UserService getUserservice() {
        return userService;
    }

    public CategoryService createCategoryService(CategoryRepository categoryRepository) {
        return new CategoryServiceImpl(categoryRepository, cacheService, productService);
    }

    public BrandService createBrandService(BrandRepository brandRepository) {
        return new BrandServiceImpl(brandRepository, cacheService, productService);
    }

    public ProductService createProductService(ProductRepository productRepository) {
        return new ProductServiceImpl(productRepository, auditService, cacheService);
    }

    @Override
    public AuditService createAuditService() {
        return new AuditServiceImpl();
    }

    @Override
    public CacheService createCacheService() {
        return new CacheService();
    }

    @Override
    public CategoryService getCategoryService() {
        return categoryService;
    }

    @Override
    public BrandService getBrandService() {
        return brandService;
    }

    @Override
    public ProductService getProductService() {
        return productService;
    }

    @Override
    public AuditService getAuditService() {
        return auditService;
    }
}
