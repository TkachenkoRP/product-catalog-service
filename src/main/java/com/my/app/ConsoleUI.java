package com.my.app;

import com.my.dto.ProductDto;
import com.my.io.ConsoleInputHandler;
import com.my.io.ConsoleOutputHandler;
import com.my.mapper.ProductMappingService;
import com.my.model.AuditLog;
import com.my.model.Brand;
import com.my.model.Category;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.model.User;
import com.my.security.UserManager;
import com.my.service.AuditService;
import com.my.service.BrandService;
import com.my.service.CategoryService;
import com.my.service.CsvDataService;
import com.my.service.ProductService;
import com.my.service.UserService;

import java.util.List;
import java.util.Map;

public class ConsoleUI {

    private final UserService userService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductService productService;
    private final ProductMappingService productMappingService;
    private final AuditService auditService;
    private final CsvDataService csvDataService;

    public ConsoleUI(ServiceFactory factory) {
        this.userService = factory.getUserservice();
        this.categoryService = factory.getCategoryService();
        this.brandService = factory.getBrandService();
        this.productService = factory.getProductService();
        this.productMappingService = new ProductMappingService(factory.getCategoryService(), factory.getBrandService());
        this.auditService = factory.getAuditService();
        this.csvDataService = factory.getCsvDataService();
    }

    public void start() {
        ConsoleOutputHandler.displayMsg("КАТАЛОГ ТОВАРОВ");

        while (true) {
            if (!UserManager.isLoggedIn()) {
                if (!showAuthMenu()) {
                    csvDataService.saveAllData();
                    break;
                }
            } else {
                showMainMenu();
            }
        }
    }

    private boolean showAuthMenu() {
        while (!UserManager.isLoggedIn()) {
            ConsoleOutputHandler.displayAuthMenu();
            String choice = ConsoleInputHandler.getUserTextInput("");

            switch (choice) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> {
                    return false;
                }
                default -> ConsoleOutputHandler.displayMsg("Неверный выбор. Попробуйте снова.");
            }
        }
        return true;
    }

    private void login() {
        ConsoleOutputHandler.displayMsg("АВТОРИЗАЦИЯ");

        int attempts = 0;
        while (attempts < 3) {
            String email = ConsoleInputHandler.getUserTextInput("Email:");
            String password = ConsoleInputHandler.getUserTextInput("Пароль:");
            User currentUser = userService.login(email, password);
            if (currentUser != null) {
                UserManager.setLoggedInUser(currentUser);
                ConsoleOutputHandler.displayMsg("Успешный вход! Добро пожаловать, " + currentUser.getUsername() + "!");
                auditService.logAction(AuditLog.AuditActions.LOGIN, "Успешный вход в систему");
                return;
            } else {
                attempts++;
                if (attempts < 3) {
                    ConsoleOutputHandler.displayMsg("Неверный логин или пароль. Попыток осталось: " + (3 - attempts));
                } else {
                    ConsoleOutputHandler.displayMsg("Превышено количество попыток входа.");
                }
            }
        }
    }

    private void register() {
        ConsoleOutputHandler.displayMsg("РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ");

        String email = ConsoleInputHandler.getUserTextInput("Введите email:");

        if (!userService.isEmailAvailable(email)) {
            ConsoleOutputHandler.displayMsg("Пользователь с таким email уже существует.");
            return;
        }

        String password = ConsoleInputHandler.getUserTextInput("Придумайте пароль:");

        String userName = ConsoleInputHandler.getUserTextInput("Ваше имя:");

        if (userService.registration(email, userName, password) != null) {
            ConsoleOutputHandler.displayMsg("Регистрация успешна! Теперь вы можете войти в систему.");
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при регистрации пользователя.");
        }
    }

    private void logout() {
        ConsoleOutputHandler.displayMsg("До свидания, " + UserManager.getLoggedInUser().getUsername() + "!");
        auditService.logAction(AuditLog.AuditActions.LOGOUT, "Выход из системы");
        UserManager.logout();
    }

    private void showMainMenu() {
        while (UserManager.isLoggedIn()) {
            ConsoleOutputHandler.displayMainMenu(UserManager.getLoggedInUser());
            String choice = ConsoleInputHandler.getUserTextInput("");

            switch (choice) {
                case "1" -> showAllProducts();
                case "2" -> searchProduct();
                case "3" -> addProduct();
                case "4" -> updateProduct();
                case "5" -> deleteProduct();
                case "6" -> showCategoriesManagement();
                case "7" -> showBrandsManagement();
                case "8" -> showUserManagement();
                case "9" -> auditService.showAuditLog();
                case "10" -> showMetrics();
                case "0" -> logout();
                default -> ConsoleOutputHandler.displayMsg("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private void showCategoriesManagement() {
        while (true) {
            ConsoleOutputHandler.displayCategoriesManagementMenu();
            String choice = ConsoleInputHandler.getUserTextInput("");

            switch (choice) {
                case "1" -> showAllCategories();
                case "2" -> addCategory();
                case "3" -> updateCategory();
                case "4" -> deleteCategory();
                case "0" -> {
                    return;
                }
                default -> ConsoleOutputHandler.displayMsg("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private void showAllCategories() {
        List<Category> categories = categoryService.getAll();
        ConsoleOutputHandler.displayCategories(categories);
        auditService.logAction(AuditLog.AuditActions.VIEW_ALL_CATEGORIES, "Просмотр всех категорий");
    }

    private void addCategory() {
        ConsoleOutputHandler.displayMsg("ДОБАВЛЕНИЕ КАТЕГОРИИ");

        String name = ConsoleInputHandler.getUserTextInput("Название категории:");

        Category category = new Category(name);
        Category createdCategory = categoryService.save(category);
        if (createdCategory != null) {
            ConsoleOutputHandler.displayMsg("Категория успешно добавлена! ID: " + createdCategory.getId());
            auditService.logAction(AuditLog.AuditActions.ADD_CATEGORY, "Категория успешна создана! ID: " + createdCategory.getId());
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при добавлении категории.");
        }
    }

    private void updateCategory() {
        ConsoleOutputHandler.displayMsg("РЕДАКТИРОВАНИЕ КАТЕГОРИИ");

        Long id = ConsoleInputHandler.getUserLongInput("Введите ID категории для редактирования:");
        Category existing = categoryService.getById(id);

        if (existing == null) {
            ConsoleOutputHandler.displayMsg("Категория с ID " + id + " не найдена.");
            return;
        }

        ConsoleOutputHandler.displayMsg("Текущие данные: " + existing);

        String name = ConsoleInputHandler.getUserTextInput("Новое название (оставьте пустым для сохранения текущего):");

        if (name.isEmpty()) {
            return;
        }

        Category updatedCategory = new Category(name);
        Category result = categoryService.update(id, updatedCategory);
        if (result != null) {
            ConsoleOutputHandler.displayMsg("Категория успешно обновлена!");
            auditService.logAction(AuditLog.AuditActions.UPDATE_CATEGORY, "Обновлена категория ID: " + id);
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при обновлении категории.");
        }
    }

    private void deleteCategory() {
        ConsoleOutputHandler.displayMsg("УДАЛЕНИЕ КАТЕГОРИИ");

        Long id = ConsoleInputHandler.getUserLongInput("Введите ID категории для удаления:");
        Category existing = categoryService.getById(id);

        if (existing == null) {
            ConsoleOutputHandler.displayMsg("Категория с ID " + id + " не найдена.");
            return;
        }

        ConsoleOutputHandler.displayMsg("Категория для удаления: " + existing);
        boolean confirm = ConsoleInputHandler.getUserConfirmation("Вы действительно хотите удалить категорию?");

        if (confirm) {
            if (categoryService.deleteById(id)) {
                ConsoleOutputHandler.displayMsg("Категория успешно удалена!");
                auditService.logAction(AuditLog.AuditActions.DELETE_CATEGORY, "Удалена категория ID: " + id);
            } else {
                ConsoleOutputHandler.displayMsg("Ошибка при удалении категории.");
            }
        } else {
            ConsoleOutputHandler.displayMsg("Удаление отменено.");
        }
    }

    private void showUserManagement() {
        while (true) {
            ConsoleOutputHandler.displayUserManagementMenu();
            String choice = ConsoleInputHandler.getUserTextInput("");

            switch (choice) {
                case "1" -> updateUserName();
                case "2" -> updateUserEmail();
                case "3" -> updateUserPassword();
                case "0" -> {
                    return;
                }
                default -> ConsoleOutputHandler.displayMsg("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private void updateUserName() {
        ConsoleOutputHandler.displayMsg("ИЗМЕНЕНИЕ ИМЕНИ");

        User currentUser = UserManager.getLoggedInUser();
        ConsoleOutputHandler.displayUserProfile(currentUser);

        String newName = ConsoleInputHandler.getUserTextInput("Новое имя:");
        if (newName.isEmpty()) {
            ConsoleOutputHandler.displayMsg("Имя не может быть пустым.");
            return;
        }

        User user = new User();
        user.setUsername(newName);

        User updatedUser = userService.update(currentUser.getId(), user);
        if (updatedUser != null) {
            UserManager.setLoggedInUser(updatedUser);
            ConsoleOutputHandler.displayMsg("Имя успешно изменено!");
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при изменении имени.");
        }
    }

    private void updateUserEmail() {
        ConsoleOutputHandler.displayMsg("ИЗМЕНЕНИЕ EMAIL");

        User currentUser = UserManager.getLoggedInUser();
        ConsoleOutputHandler.displayUserProfile(currentUser);

        String newEmail = ConsoleInputHandler.getUserTextInput("Новый email:");
        if (newEmail.isEmpty()) {
            ConsoleOutputHandler.displayMsg("Email не может быть пустым.");
            return;
        }

        if (!userService.isEmailAvailable(newEmail) && !newEmail.equalsIgnoreCase(currentUser.getEmail())) {
            ConsoleOutputHandler.displayMsg("Пользователь с таким email уже существует.");
            return;
        }

        User user = new User();
        user.setEmail(newEmail);

        User updatedUser = userService.update(currentUser.getId(), user);
        if (updatedUser != null) {
            UserManager.setLoggedInUser(updatedUser);
            ConsoleOutputHandler.displayMsg("Email успешно изменен!");
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при изменении email.");
        }
    }

    private void updateUserPassword() {
        ConsoleOutputHandler.displayMsg("ИЗМЕНЕНИЕ ПАРОЛЯ");

        User currentUser = UserManager.getLoggedInUser();

        String currentPassword = ConsoleInputHandler.getUserTextInput("Текущий пароль:");
        if (!currentPassword.equals(currentUser.getPassword())) {
            ConsoleOutputHandler.displayMsg("Неверный текущий пароль.");
            return;
        }

        String newPassword = ConsoleInputHandler.getUserTextInput("Новый пароль:");
        String confirmPassword = ConsoleInputHandler.getUserTextInput("Подтвердите новый пароль:");

        if (!newPassword.equals(confirmPassword)) {
            ConsoleOutputHandler.displayMsg("Пароли не совпадают.");
            return;
        }

        User user = new User();
        user.setPassword(newPassword);

        User updatedUser = userService.update(currentUser.getId(), user);
        if (updatedUser != null) {
            UserManager.setLoggedInUser(updatedUser);
            ConsoleOutputHandler.displayMsg("Пароль успешно изменен!");
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при изменении пароля.");
        }
    }

    private void showBrandsManagement() {
        while (true) {
            ConsoleOutputHandler.displayBrandsManagementMenu();
            String choice = ConsoleInputHandler.getUserTextInput("");

            switch (choice) {
                case "1" -> showAllBrands();
                case "2" -> addBrand();
                case "3" -> updateBrand();
                case "4" -> deleteBrand();
                case "0" -> {
                    return;
                }
                default -> ConsoleOutputHandler.displayMsg("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private void showAllBrands() {
        List<Brand> brands = brandService.getAll();
        ConsoleOutputHandler.displayBrands(brands);
        auditService.logAction(AuditLog.AuditActions.VIEW_ALL_BRANDS, "Просмотр всех брендов");
    }

    private void addBrand() {
        ConsoleOutputHandler.displayMsg("ДОБАВЛЕНИЕ БРЕНДА");

        String name = ConsoleInputHandler.getUserTextInput("Название бренда:");

        Brand brand = new Brand(name);

        Brand createdBrand = brandService.save(brand);
        if (createdBrand != null) {
            ConsoleOutputHandler.displayMsg("Бренд успешно добавлен! ID: " + createdBrand.getId());
            auditService.logAction(AuditLog.AuditActions.ADD_BRAND, "Добавлен бренд: " + createdBrand.getName());
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при добавлении бренда.");
        }
    }

    private void updateBrand() {
        ConsoleOutputHandler.displayMsg("РЕДАКТИРОВАНИЕ БРЕНДА");

        Long id = ConsoleInputHandler.getUserLongInput("Введите ID бренда для редактирования:");
        Brand existing = brandService.getById(id);

        if (existing == null) {
            ConsoleOutputHandler.displayMsg("Бренд с ID " + id + " не найден.");
            return;
        }

        ConsoleOutputHandler.displayMsg("Текущие данные: " + existing);

        String name = ConsoleInputHandler.getUserTextInput("Новое название (оставьте пустым для сохранения текущего):");

        if (name.isEmpty()) {
            return;
        }

        Brand updatedBrand = new Brand(name);
        Brand result = brandService.update(id, updatedBrand);
        if (result != null) {
            ConsoleOutputHandler.displayMsg("Бренд успешно обновлен!");
            auditService.logAction(AuditLog.AuditActions.UPDATE_BRAND, "Обновлен бренд ID: " + id);
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при обновлении бренда.");
        }
    }

    private void deleteBrand() {
        ConsoleOutputHandler.displayMsg("УДАЛЕНИЕ БРЕНДА");

        Long id = ConsoleInputHandler.getUserLongInput("Введите ID бренда для удаления:");
        Brand existing = brandService.getById(id);

        if (existing == null) {
            ConsoleOutputHandler.displayMsg("Бренд с ID " + id + " не найден.");
            return;
        }

        ConsoleOutputHandler.displayMsg("Бренд для удаления: " + existing);
        boolean confirm = ConsoleInputHandler.getUserConfirmation("Вы действительно хотите удалить бренд?");

        if (confirm) {
            if (brandService.deleteById(id)) {
                ConsoleOutputHandler.displayMsg("Бренд успешно удален!");
                auditService.logAction(AuditLog.AuditActions.DELETE_BRAND, "Удален бренд ID: " + id);
            } else {
                ConsoleOutputHandler.displayMsg("Ошибка при удалении бренда.");
            }
        } else {
            ConsoleOutputHandler.displayMsg("Удаление отменено.");
        }
    }

    private void showAllProducts() {
        List<Product> products = productService.getAll(null);
        List<ProductDto> productDtoList = getProductDtos(products);
        ConsoleOutputHandler.displayDtoProducts(productDtoList, "ВСЕ ТОВАРЫ");
    }

    private List<ProductDto> getProductDtos(List<Product> products) {
        return products.stream().map(productMappingService::toDto).toList();
    }

    private void searchProduct() {
        List<Product> results = performSearch();
        if (results != null) {
            List<ProductDto> productDtoList = getProductDtos(results);
            ConsoleOutputHandler.displayDtoProducts(productDtoList, "РЕЗУЛЬТАТЫ ПОИСКА");
        }
    }

    private List<Product> performSearch() {
        ConsoleOutputHandler.displayMsg("Задайте параметры фильтра: ");

        boolean useCategory = ConsoleInputHandler.getUserConfirmation("Использовать фильтр по категории?");
        Long categoryId = null;
        if (useCategory) {
            categoryId = ConsoleInputHandler.getUserLongInput("Введите ID категории:");
        }

        boolean useBrand = ConsoleInputHandler.getUserConfirmation("Использовать фильтр по бренду?");
        Long brandId = null;
        if (useBrand) {
            brandId = ConsoleInputHandler.getUserLongInput("Введите ID бренда:");
        }

        boolean useMinPrice = ConsoleInputHandler.getUserConfirmation("Использовать фильтр по минимальной цене?");
        Double minPrice = null;
        if (useMinPrice) {
            minPrice = ConsoleInputHandler.getUserDoubleInput("Минимальная цена:");
        }

        boolean useMaxPrice = ConsoleInputHandler.getUserConfirmation("Использовать фильтр по максимальной цене?");
        Double maxPrice = null;
        if (useMaxPrice) {
            maxPrice = ConsoleInputHandler.getUserDoubleInput("Максимальная цена:");
        }

        boolean useStock = ConsoleInputHandler.getUserConfirmation("Использовать фильтр по остаткам?");
        Integer stock = null;
        if (useStock) {
            stock = ConsoleInputHandler.getUserIntegerInput("минимальный остаток:");
        }

        ProductFilter filter = new ProductFilter(categoryId, brandId, minPrice, maxPrice, stock);

        return productService.getAll(filter);
    }

    private void addProduct() {
        ConsoleOutputHandler.displayMsg("ДОБАВЛЕНИЕ ТОВАРА");

        String name = ConsoleInputHandler.getUserTextInput("Название:");

        List<Category> categories = categoryService.getAll();
        ConsoleOutputHandler.displayCategories(categories);
        Long categoryId = ConsoleInputHandler.getUserLongInput("ID категории:");

        List<Brand> brands = brandService.getAll();
        ConsoleOutputHandler.displayBrands(brands);
        Long brandId = ConsoleInputHandler.getUserLongInput("ID бренда:");

        double price = ConsoleInputHandler.getUserDoubleInput("Цена:");
        int stock = ConsoleInputHandler.getUserIntegerInput("Количество на складе:");

        Product product = new Product();
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setBrandId(brandId);
        product.setPrice(price);
        product.setStock(stock);
        Product saved = productService.save(product);
        if (saved != null) {
            ConsoleOutputHandler.displayMsg("Товар успешно добавлен! ID: " + saved.getId());
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при добавлении бренда.");
        }
    }

    private void updateProduct() {
        ConsoleOutputHandler.displayMsg("РЕДАКТИРОВАНИЕ ТОВАРА");

        Long id = ConsoleInputHandler.getUserLongInput("Введите ID товара для редактирования:");
        Product existing = productService.getById(id);

        if (existing == null) {
            ConsoleOutputHandler.displayMsg("Товар с ID " + id + " не найден.");
            return;
        }

        ConsoleOutputHandler.displayDtoProducts(getProductDtos(List.of(existing)), "ТЕКУЩИЙ ТОВАР");
        Product sourceProduct = getSourceProduct();

        Product updated = productService.update(id, sourceProduct);

        if (updated != null) {
            ConsoleOutputHandler.displayMsg("Товар успешно обновлен!");
        } else {
            ConsoleOutputHandler.displayMsg("Ошибка при обновлении товара.");
        }
    }

    private Product getSourceProduct() {
        ConsoleOutputHandler.displayMsg("Оставьте поле пустым, чтобы не изменять значение.");

        Product sourceProduct = new Product();

        String name = ConsoleInputHandler.getUserTextInput("Новое название:");
        if (!name.isEmpty()) {
            sourceProduct.setName(name);
        }

        String categoryIdStr = ConsoleInputHandler.getUserTextInput("Новый ID категории:");
        if (!categoryIdStr.isEmpty()) {
            Long categoryId = Long.parseLong(categoryIdStr);
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                sourceProduct.setCategoryId(category.getId());
            } else {
                ConsoleOutputHandler.displayMsg("Категория не найдена. Категория не изменена.");
            }
        }

        String brandIdStr = ConsoleInputHandler.getUserTextInput("Новый ID бренда:");
        if (!brandIdStr.isEmpty()) {
            Long brandId = Long.parseLong(brandIdStr);
            Brand brand = brandService.getById(brandId);
            if (brand != null) {
                sourceProduct.setBrandId(brand.getId());
            } else {
                ConsoleOutputHandler.displayMsg("Бренд не найден. Бренд не изменен.");
            }
        }

        String priceStr = ConsoleInputHandler.getUserTextInput("Новая цена:");
        if (!priceStr.isEmpty()) {
            sourceProduct.setPrice(Double.parseDouble(priceStr));
        }

        String stockStr = ConsoleInputHandler.getUserTextInput("Новое количество:");
        if (!stockStr.isEmpty()) {
            sourceProduct.setStock(Integer.parseInt(stockStr));
        }

        return sourceProduct;
    }

    private void deleteProduct() {
        ConsoleOutputHandler.displayMsg("УДАЛЕНИЕ ТОВАРА");

        Long id = ConsoleInputHandler.getUserLongInput("Введите ID товара для удаления:");
        Product existing = productService.getById(id);

        if (existing == null) {
            ConsoleOutputHandler.displayMsg("Товар с ID " + id + " не найден.");
            return;
        }

        ConsoleOutputHandler.displayDtoProducts(getProductDtos(List.of(existing)), "ТОВАР ДЛЯ УДАЛЕНИЯ");
        boolean confirm = ConsoleInputHandler.getUserConfirmation("Вы действительно хотите удалить товар?");

        if (confirm) {
            if (productService.deleteById(id)) {
                ConsoleOutputHandler.displayMsg("Товар успешно удален!");
            } else {
                ConsoleOutputHandler.displayMsg("Ошибка при удалении товара.");
            }
        } else {
            ConsoleOutputHandler.displayMsg("Удаление отменено.");
        }
    }

    private void showMetrics() {
        Map<String, Long> metrics = productService.getMetrics();
        ConsoleOutputHandler.displayMetrics(metrics);
    }
}
