package com.my.io;

import com.my.dto.ProductDto;
import com.my.model.Brand;
import com.my.model.Category;
import com.my.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConsoleOutputHandler {
    private ConsoleOutputHandler() {
    }

    public static void displayMsg(String msg) {
        System.out.println(msg);
    }

    public static void displayAuthMenu() {
        String menu = """
                МЕНЮ АВТОРИЗАЦИИ
                
                1. Вход в систему
                2. Регистрация
                0. Выход из приложения
                
                Выберите действие:\s""";

        System.out.print(menu);
    }

    public static void displayMainMenu(User currentUser) {
        String menu = """
                ГЛАВНОЕ МЕНЮ
                Текущий пользователь: %s
                
                1. Просмотр всех товаров
                2. Поиск товаров
                3. Добавить товар
                4. Редактировать товар
                5. Удалить товар
                6. Управление категориями
                7. Управление брендами
                8. Управление профилем
                9. Аудит лог
                10. Показать метрики
                
                0. Выход
                
                Выберите действие:\s""".formatted(currentUser.getUsername());

        System.out.print(menu);
    }

    public static void displayUserManagementMenu() {
        String menu = """
                УПРАВЛЕНИЕ ПРОФИЛЕМ
                
                1. Изменить имя
                2. Изменить email
                3. Изменить пароль
                0. Назад
                
                Выберите действие:\s""";

        System.out.print(menu);
    }

    public static void displayCategoriesManagementMenu() {
        String menu = """
                УПРАВЛЕНИЕ КАТЕГОРИЯМИ
                
                1. Просмотр всех категорий
                2. Добавить категорию
                3. Редактировать категорию
                4. Удалить категорию
                0. Назад
                
                Выберите действие:\s""";

        System.out.print(menu);
    }

    public static void displayCategories(List<Category> categories) {
        displayMsg("ДОСТУПНЫЕ КАТЕГОРИИ");
        if (categories.isEmpty()) {
            displayMsg("Категории отсутствуют");
        } else {
            categories.forEach(category ->
                    displayMsg("  • " + category.getId() + " - " + category.getName()));

        }
    }

    public static void displayUserProfile(User user) {
        displayMsg("ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ");
        displayMsg("  ID: " + user.getId());
        displayMsg("  Имя: " + user.getUsername());
        displayMsg("  Email: " + user.getEmail());
    }

    public static void displayBrandsManagementMenu() {
        String menu = """
                УПРАВЛЕНИЕ БРЕНДАМИ
                
                1. Просмотр всех брендов
                2. Добавить бренд
                3. Редактировать бренд
                4. Удалить бренд
                0. Назад
                
                Выберите действие:\s""";

        System.out.print(menu);
    }

    public static void displayBrands(List<Brand> brands) {
        displayMsg("ДОСТУПНЫЕ БРЕНДЫ");
        if (brands.isEmpty()) {
            displayMsg("Бренды отсутствуют");
        } else {
            brands.forEach(brand ->
                    displayMsg("  • " + brand.getId() + " - " + brand.getName())
            );
        }
    }

    public static void displayDtoProducts(List<ProductDto> products, String title) {
        displayMsg(title);

        if (products == null || products.isEmpty()) {
            displayMsg("Товары не найдены.");
        } else {
            products.forEach(product ->
                    displayMsg("  " + String.format("ID: %s | Наименование: %s | Категории: %s | Бренд: %s | Цена: %.2f | Остаток: %d",
                            product.getId(),
                            product.getName(),
                            product.getCategory().getName(),
                            product.getBrand().getName(),
                            product.getPrice(),
                            product.getStock())));
            displayMsg("\nНайдено товаров: " + products.size());
        }
    }

    public static void displayMetrics(Map<String, Long> metrics) {
        displayMsg("МЕТРИКИ");
        if (metrics.isEmpty()) {
            System.out.println("Метрики отсутствуют");
        } else {
            metrics.forEach((operation, nanos) -> {
                double millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                System.out.printf("%s: %.2f ms\n", operation, millis);
            });
        }
    }
}
