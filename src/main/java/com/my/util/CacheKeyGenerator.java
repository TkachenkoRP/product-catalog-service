package com.my.util;

public class CacheKeyGenerator {
    private CacheKeyGenerator() {
    }

    public static String generateProductKey(Long id) {
        return "product:" + id;
    }

    public static String generateAllProductsKey() {
        return "products:all";
    }

    public static String generateCategoryKey(Long id) {
        return "category:" + id;
    }

    public static String generateAllCategoriesKey() {
        return "categories:all";
    }

    public static String generateBrandKey(Long id) {
        return "brand:" + id;
    }

    public static String generateAllBrandsKey() {
        return "brands:all";
    }
}
