package com.my.app;

import com.my.service.AuditService;
import com.my.service.BrandService;
import com.my.service.CategoryService;
import com.my.service.ProductService;
import com.my.service.UserService;

/**
 * Фабрика для создания сервисов приложения.
 */
public interface ServiceFactory {
    /**
     * Возвращает сервис для работы с пользователями.
     *
     * @return экземпляр {@link UserService}
     */
    UserService getUserservice();

    /**
     * Возвращает сервис для работы с категориями.
     *
     * @return экземпляр {@link CategoryService}
     */
    CategoryService getCategoryService();

    /**
     * Возвращает сервис для работы с брендами.
     *
     * @return экземпляр {@link BrandService}
     */
    BrandService getBrandService();

    /**
     * Возвращает сервис для работы с товарами.
     *
     * @return экземпляр {@link ProductService}
     */
    ProductService getProductService();

    /**
     * Возвращает сервис для ведения аудит-лога.
     *
     * @return экземпляр {@link AuditService}
     */
    AuditService getAuditService();
}
