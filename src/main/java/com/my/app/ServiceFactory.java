package com.my.app;

import com.my.service.AuditService;
import com.my.service.BrandService;
import com.my.service.CacheService;
import com.my.service.CategoryService;
import com.my.service.CsvDataService;
import com.my.service.ProductService;
import com.my.service.UserService;

public interface ServiceFactory {
    UserService getUserservice();

    CategoryService getCategoryService();

    BrandService getBrandService();

    ProductService getProductService();

    AuditService getAuditService();

    CacheService getCacheService();

    CsvDataService getCsvDataService();
}
