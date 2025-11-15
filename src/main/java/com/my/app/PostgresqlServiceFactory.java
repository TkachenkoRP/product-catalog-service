package com.my.app;

import com.my.configuration.AppConfiguration;
import com.my.io.ConsoleOutputHandler;
import com.my.repository.BrandRepository;
import com.my.repository.CategoryRepository;
import com.my.repository.ProductRepository;
import com.my.repository.UserRepository;
import com.my.repository.impl.PostgresqlBrandRepositoryImpl;
import com.my.repository.impl.PostgresqlCategoryRepositoryImpl;
import com.my.repository.impl.PostgresqlProductRepositoryImpl;
import com.my.repository.impl.PostgresqlUserRepositoryImpl;
import com.my.service.AuditService;
import com.my.service.BrandService;
import com.my.service.CacheService;
import com.my.service.CategoryService;
import com.my.service.ProductService;
import com.my.service.UserService;
import com.my.service.impl.AuditServiceImpl;
import com.my.service.impl.BrandServiceImpl;
import com.my.service.impl.CategoryServiceImpl;
import com.my.service.impl.ProductServiceImpl;
import com.my.service.impl.UserServiceImpl;
import com.my.util.DBUtil;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgresqlServiceFactory implements ServiceFactory, ServiceCreator {

    private final AuditService auditService;
    private final CacheService cacheService;

    private final UserService userService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductService productService;

    public PostgresqlServiceFactory() {
        this.cacheService = createCacheService();
        this.auditService = createAuditService();

        UserRepository userRepository = new PostgresqlUserRepositoryImpl();
        CategoryRepository categoryRepository = new PostgresqlCategoryRepositoryImpl();
        BrandRepository brandRepository = new PostgresqlBrandRepositoryImpl();
        ProductRepository productRepository = new PostgresqlProductRepositoryImpl();

        this.productService = createProductService(productRepository);
        this.userService = createUserService(userRepository);
        this.categoryService = createCategoryService(categoryRepository);
        this.brandService = createBrandService(brandRepository);

        initPostgresql();
    }

    private void initPostgresql() {
        try {
            Connection connection = DBUtil.getConnection(AppConfiguration.getProperty("liquibase.liquibase-schema"));
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(AppConfiguration.getProperty("liquibase.change-log"), new ClassLoaderResourceAccessor(), database);
            liquibase.update(AppConfiguration.getProperty("liquibase.contexts"));
        } catch (LiquibaseException e) {
            ConsoleOutputHandler.displayMsg("Ошибка инициализации БД!");
            System.err.println(e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create connection database: " + e.getMessage());
        }
    }

    @Override
    public UserService createUserService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    @Override
    public CategoryService createCategoryService(CategoryRepository categoryRepository) {
        return new CategoryServiceImpl(categoryRepository, cacheService, productService);
    }

    @Override
    public BrandService createBrandService(BrandRepository brandRepository) {
        return new BrandServiceImpl(brandRepository, cacheService, productService);
    }

    @Override
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
    public UserService getUserservice() {
        return userService;
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
