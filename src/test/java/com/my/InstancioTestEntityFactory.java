package com.my;

import com.my.dto.UserLoginRequestDto;
import com.my.dto.UserRegisterRequestDto;
import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.model.Brand;
import com.my.model.Category;
import com.my.model.Product;
import com.my.model.User;
import com.my.model.UserRole;
import org.instancio.Instancio;
import org.instancio.Model;

import java.util.List;

import static org.instancio.Select.field;

/**
 * Фабрика тестовых сущностей с использованием Instancio.
 * Создает тестовые объекты с реалистичными данными для использования в тестах.
 */
public class InstancioTestEntityFactory {

    private InstancioTestEntityFactory() {
    }

    private static final Model<User> USER_MODEL = Instancio.of(User.class)
            .generate(field(User::getEmail), gen -> gen.net().email())
            .generate(field(User::getPassword), gen -> gen.string().length(6))
            .toModel();

    /**
     * Создает тестовый бренд с указанным ID.
     */
    public static Brand createBrand(Long id) {
        return Instancio.of(Brand.class)
                .set(field(Brand::getId), id)
                .create();
    }

    /**
     * Создает список тестовых брендов заданного размера.
     */
    public static List<Brand> createBrandList(int size) {
        return Instancio.ofList(Brand.class)
                .size(size)
                .create();
    }

    /**
     * Создает тестовую категорию с указанным ID.
     */
    public static Category createCategory(Long id) {
        return Instancio.of(Category.class)
                .set(field(Category::getId), id)
                .create();
    }

    /**
     * Создает список тестовых категорий заданного размера.
     */
    public static List<Category> createCategoryList(int size) {
        return Instancio.ofList(Category.class)
                .size(size)
                .create();
    }

    /**
     * Создает тестовый товар с указанным ID.
     */
    public static Product createProduct(Long id) {
        return Instancio.of(Product.class)
                .set(field(Product::getId), id)
                .create();
    }

    /**
     * Создает список тестовых товаров.
     */
    public static List<Product> createProductList() {
        return Instancio.createList(Product.class);
    }

    /**
     * Создает тестового пользователя.
     */
    public static User createUser() {
        return Instancio.create(USER_MODEL);
    }

    /**
     * Создает тестового пользователя с указанным ID.
     */
    public static User createUser(Long id) {
        return Instancio.of(USER_MODEL)
                .set(field(User::getId), id)
                .create();
    }

    /**
     * Создает тестового пользователя с указанным ID и ролью.
     */
    public static User createUser(Long id, UserRole role) {
        return Instancio.of(USER_MODEL)
                .set(field(User::getId), id)
                .set(field(User::getRole), role)
                .create();
    }

    /**
     * Создает список тестовых пользователей заданного размера.
     */
    public static List<User> createUserList(int size) {
        return Instancio.ofList(USER_MODEL)
                .size(size)
                .create();
    }

    /**
     * Создает тестового администратора.
     */
    public static User createAdminUser() {
        return createUser(null, UserRole.ROLE_ADMIN);
    }

    /**
     * Создает тестового администратора с указанным ID.
     */
    public static User createAdminUser(Long id) {
        return createUser(id, UserRole.ROLE_ADMIN);
    }

    /**
     * Создает тестового обычного пользователя.
     */
    public static User createRegularUser() {
        return createUser(null, UserRole.ROLE_USER);
    }

    /**
     * Создает тестового обычного пользователя с указанным ID.
     */
    public static User createRegularUser(Long id) {
        return createUser(id, UserRole.ROLE_USER);
    }

    /**
     * Создает DTO ответа для пользователя на основе объекта User.
     */
    public static UserResponseDto createUserResponseDto(User user) {
        return Instancio.of(getUserResponseDtoModel(user)).create();
    }

    /**
     * Создает DTO запроса для пользователя на основе объекта User.
     */
    public static UserRequestDto createUserRequestDto(User user) {
        return Instancio.of(getUserRequestDtoModel(user)).create();
    }

    /**
     * Создает DTO входа для пользователя на основе объекта User.
     */
    public static UserLoginRequestDto createUserLoginRequestDto(User user) {
        return Instancio.of(getUserLoginRequestDtoModel(user)).create();
    }

    /**
     * Создает DTO регистрации для пользователя на основе объекта User.
     */
    public static UserRegisterRequestDto createUserRegisterRequestDto(User user) {
        return Instancio.of(getUserRegisterRequestDtoModel(user)).create();
    }

    /**
     * Создает DTO входа со случайными данными.
     */
    public static UserLoginRequestDto createUserLoginRequestDto() {
        return Instancio.of(UserLoginRequestDto.class)
                .generate(field(UserLoginRequestDto::email), gen -> gen.net().email())
                .generate(field(UserLoginRequestDto::password), gen -> gen.string().length(6))
                .create();
    }

    /**
     * Создает список DTO ответов для пользователей на основе списка User.
     */
    public static List<UserResponseDto> createUserResponseDtos(List<User> users) {
        return users.stream()
                .map(InstancioTestEntityFactory::createUserResponseDto)
                .toList();
    }


    private static Model<UserRequestDto> getUserRequestDtoModel(User user) {
        return Instancio.of(UserRequestDto.class)
                .set(field(UserRequestDto::email), user.getEmail())
                .set(field(UserRequestDto::username), user.getUsername())
                .set(field(UserRequestDto::password), user.getPassword())
                .toModel();
    }

    private static Model<UserResponseDto> getUserResponseDtoModel(User user) {
        return Instancio.of(UserResponseDto.class)
                .set(field(UserResponseDto::id), user.getId())
                .set(field(UserResponseDto::email), user.getEmail())
                .set(field(UserResponseDto::username), user.getUsername())
                .set(field(UserResponseDto::role), user.getRole().name())
                .toModel();
    }

    private static Model<UserLoginRequestDto> getUserLoginRequestDtoModel(User user) {
        return Instancio.of(UserLoginRequestDto.class)
                .set(field(UserLoginRequestDto::email), user.getEmail())
                .set(field(UserLoginRequestDto::password), user.getPassword())
                .toModel();
    }

    private static Model<UserRegisterRequestDto> getUserRegisterRequestDtoModel(User user) {
        return Instancio.of(UserRegisterRequestDto.class)
                .set(field(UserRegisterRequestDto::email), user.getEmail())
                .set(field(UserRegisterRequestDto::password), user.getPassword())
                .set(field(UserRegisterRequestDto::username), user.getUsername())
                .toModel();
    }
}
