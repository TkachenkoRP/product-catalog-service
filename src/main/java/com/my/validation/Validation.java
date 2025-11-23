package com.my.validation;

import com.my.dto.BrandRequestDto;
import com.my.dto.CategoryRequestDto;
import com.my.dto.ProductRequestDto;
import com.my.dto.UserRequestDto;
import com.my.exception.ArgumentNotValidException;

import java.util.regex.Pattern;

public class Validation {
    private Validation() {
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я0-9\\s-]{2,255}$");

    public static void validateUserRegistration(UserRequestDto user) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (user.email() == null || user.email().isBlank()) {
            msg.append("Email не может быть пустым! ");
        } else if (!EMAIL_PATTERN.matcher(user.email()).matches()) {
            msg.append("Некорректный формат email! ");
        }

        if (user.username() == null || user.username().isBlank()) {
            msg.append("Имя пользователя не может быть пустым! ");
        } else if (user.username().length() < 2 || user.username().length() > 50) {
            msg.append("Имя пользователя должно быть от 2 до 50 символов! ");
        }

        if (user.password() == null || user.password().isBlank()) {
            msg.append("Пароль не может быть пустым! ");
        } else if (user.password().length() < 3) {
            msg.append("Пароль должен содержать минимум 3 символа! ");
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }

    public static void validateUserUpdate(UserRequestDto user) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (user.email() != null && !user.email().isBlank() && !EMAIL_PATTERN.matcher(user.email()).matches()) {
            msg.append("Некорректный формат email! ");
        }

        if (user.username() != null && !user.username().isBlank() &&
            (user.username().length() < 2 || user.username().length() > 50)) {
            msg.append("Имя пользователя должно быть от 2 до 50 символов! ");
        }

        if (user.password() != null && !user.password().isBlank() && user.password().length() < 3) {
            msg.append("Пароль должен содержать минимум 3 символа! ");
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }

    public static void validateCategoryCreate(CategoryRequestDto category) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (category.name() == null || category.name().isBlank()) {
            msg.append("Название категории не может быть пустым! ");
        } else if (category.name().length() < 2 || category.name().length() > 255) {
            msg.append("Название категории должно быть от 2 до 255 символов! ");
        } else if (!NAME_PATTERN.matcher(category.name()).matches()) {
            msg.append("Название категории содержит недопустимые символы! ");
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }

    public static void validateCategoryUpdate(CategoryRequestDto category) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (category.name() != null && !category.name().isBlank()) {
            if (category.name().length() < 2 || category.name().length() > 255) {
                msg.append("Название категории должно быть от 2 до 255 символов! ");
            } else if (!NAME_PATTERN.matcher(category.name()).matches()) {
                msg.append("Название категории содержит недопустимые символы! ");
            }
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }

    public static void validateBrandCreate(BrandRequestDto brand) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (brand.name() == null || brand.name().isBlank()) {
            msg.append("Название бренда не может быть пустым! ");
        } else if (brand.name().length() < 2 || brand.name().length() > 255) {
            msg.append("Название бренда должно быть от 2 до 255 символов! ");
        } else if (!NAME_PATTERN.matcher(brand.name()).matches()) {
            msg.append("Название бренда содержит недопустимые символы! ");
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }

    public static void validateBrandUpdate(BrandRequestDto brand) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (brand.name() != null && !brand.name().isBlank()) {
            if (brand.name().length() < 2 || brand.name().length() > 255) {
                msg.append("Название бренда должно быть от 2 до 255 символов! ");
            } else if (!NAME_PATTERN.matcher(brand.name()).matches()) {
                msg.append("Название бренда содержит недопустимые символы! ");
            }
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }

    public static void validateProductCreate(ProductRequestDto product) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (product.name() == null || product.name().isBlank()) {
            msg.append("Название товара не может быть пустым! ");
        } else if (product.name().length() < 2 || product.name().length() > 255) {
            msg.append("Название товара должно быть от 2 до 255 символов! ");
        }

        if (product.categoryId() == null) {
            msg.append("ID категории обязателен! ");
        } else if (product.categoryId() < 1) {
            msg.append("ID категории должен быть положительным числом! ");
        }

        if (product.brandId() == null) {
            msg.append("ID бренда обязателен! ");
        } else if (product.brandId() < 1) {
            msg.append("ID бренда должен быть положительным числом! ");
        }

        if (product.price() == null) {
            msg.append("Цена обязательна! ");
        } else if (product.price() <= 0) {
            msg.append("Цена должна быть больше 0! ");
        }

        if (product.stock() == null) {
            msg.append("Количество на складе обязательно! ");
        } else if (product.stock() < 0) {
            msg.append("Количество на складе не может быть отрицательным! ");
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }

    public static void validateProductUpdate(ProductRequestDto product) throws ArgumentNotValidException {
        StringBuilder msg = new StringBuilder();

        if (product.name() != null && !product.name().isBlank() &&
            (product.name().length() < 2 || product.name().length() > 255)) {
            msg.append("Название товара должно быть от 2 до 255 символов! ");
        }

        if (product.categoryId() != null && product.categoryId() < 1) {
            msg.append("ID категории должен быть положительным числом! ");
        }

        if (product.brandId() != null && product.brandId() < 1) {
            msg.append("ID бренда должен быть положительным числом! ");
        }

        if (product.price() != null && product.price() <= 0) {
            msg.append("Цена должна быть больше 0! ");
        }

        if (product.stock() != null && product.stock() < 0) {
            msg.append("Количество на складе не может быть отрицательным! ");
        }

        if (!msg.isEmpty()) {
            throw new ArgumentNotValidException(msg.toString().trim());
        }
    }
}
