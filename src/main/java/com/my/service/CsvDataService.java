package com.my.service;

import com.my.model.Brand;
import com.my.model.Category;
import com.my.model.Product;
import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.BrandRepository;
import com.my.repository.CategoryRepository;
import com.my.repository.ProductRepository;
import com.my.repository.UserRepository;
import com.my.repository.impl.InMemoryBrandRepositoryImpl;
import com.my.repository.impl.InMemoryCategoryRepositoryImpl;
import com.my.repository.impl.InMemoryProductRepositoryImpl;
import com.my.repository.impl.InMemoryUserRepositoryImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvDataService {
    private static final String DATA_DIR = "data";
    private static final String USERS_CSV = "users.csv";
    private static final String CATEGORIES_CSV = "categories.csv";
    private static final String BRANDS_CSV = "brands.csv";
    private static final String PRODUCTS_CSV = "products.csv";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public CsvDataService(UserRepository userRepository, CategoryRepository categoryRepository, BrandRepository brandRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при создании директории данных: " + e.getMessage());
        }
    }

    public void saveAllData() {
        try {
            saveUsers();
            saveCategories();
            saveBrands();
            saveProducts();
            System.out.println("Все данные успешно сохранены в CSV файлы");
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public void loadAllData() {
        try {
            loadUsers();
            loadCategories();
            loadBrands();
            loadProducts();
            System.out.println("Все данные успешно загружены из CSV файлов");
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    private void saveUsers() throws IOException {
        List<User> users = userRepository.getAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + File.separator + USERS_CSV))) {
            writer.println("id;email;username;password;role");
            for (User user : users) {
                writer.printf("%d;%s;%s;%s;%s%n",
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getRole()
                );
            }
        }
    }

    private void saveCategories() throws IOException {
        List<Category> categories = categoryRepository.getAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + File.separator + CATEGORIES_CSV))) {
            writer.println("id;name");
            for (Category category : categories) {
                writer.printf("%d;%s%n",
                        category.getId(),
                        category.getName()
                );
            }
        }
    }

    private void saveBrands() throws IOException {
        List<Brand> brands = brandRepository.getAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + File.separator + BRANDS_CSV))) {
            writer.println("id;name");
            for (Brand brand : brands) {
                writer.printf("%d;%s%n",
                        brand.getId(),
                        brand.getName()
                );
            }
        }
    }

    private void saveProducts() throws IOException {
        List<Product> products = productRepository.getAll(null);
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + File.separator + PRODUCTS_CSV))) {
            writer.println("id;name;category_id;brand_id;price;stock");
            for (Product product : products) {
                writer.printf("%d;%s;%d;%d;%.2f;%d%n",
                        product.getId(),
                        product.getName(),
                        product.getCategoryId(),
                        product.getBrandId(),
                        product.getPrice(),
                        product.getStock()
                );
            }
        }
    }

    private void loadUsers() throws IOException {
        File file = new File(DATA_DIR + File.separator + USERS_CSV);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<User> users = new ArrayList<>();
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";", -1);
                if (fields.length >= 5) {
                    User user = new User(
                            Long.parseLong(fields[0]),
                            fields[1],
                            fields[2],
                            fields[3],
                            UserRole.valueOf(fields[4])
                    );
                    users.add(user);
                }
            }
            ((InMemoryUserRepositoryImpl) userRepository).loadData(users);
        }
    }

    private void loadCategories() throws IOException {
        File file = new File(DATA_DIR + File.separator + CATEGORIES_CSV);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Category> categories = new ArrayList<>();
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";", -1);
                if (fields.length >= 2) {
                    Category category = new Category(
                            Long.parseLong(fields[0]),
                            fields[1]
                    );
                    categories.add(category);
                }
            }
            ((InMemoryCategoryRepositoryImpl) categoryRepository).loadData(categories);
        }
    }

    private void loadBrands() throws IOException {
        File file = new File(DATA_DIR + File.separator + BRANDS_CSV);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Brand> brands = new ArrayList<>();
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";", -1);
                if (fields.length >= 2) {
                    Brand brand = new Brand(
                            Long.parseLong(fields[0]),
                            fields[1]
                    );
                    brands.add(brand);
                }
            }
            ((InMemoryBrandRepositoryImpl) brandRepository).loadData(brands);
        }
    }

    private void loadProducts() throws IOException {
        File file = new File(DATA_DIR + File.separator + PRODUCTS_CSV);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Product> products = new ArrayList<>();
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";", -1);
                if (fields.length >= 6) {
                    Product product = new Product(
                            Long.parseLong(fields[0]),
                            fields[1],
                            Long.parseLong(fields[2]),
                            Long.parseLong(fields[3]),
                            parseDouble(fields[4]),
                            Integer.parseInt(fields[5])
                    );
                    products.add(product);
                }
            }
            ((InMemoryProductRepositoryImpl) productRepository).loadData(products);
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        String normalized = value.replace(',', '.').replace(" ", "");
        return Double.parseDouble(normalized);
    }

    public boolean hasSavedData() {
        return Files.exists(Paths.get(DATA_DIR + File.separator + USERS_CSV)) ||
               Files.exists(Paths.get(DATA_DIR + File.separator + CATEGORIES_CSV)) ||
               Files.exists(Paths.get(DATA_DIR + File.separator + BRANDS_CSV)) ||
               Files.exists(Paths.get(DATA_DIR + File.separator + PRODUCTS_CSV));
    }
}
