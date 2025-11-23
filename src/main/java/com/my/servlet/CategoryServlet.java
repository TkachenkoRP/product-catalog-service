package com.my.servlet;

import com.my.annotation.Loggable;
import com.my.dto.ApiResponse;
import com.my.dto.CategoryRequestDto;
import com.my.dto.CategoryResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.service.CategoryService;
import com.my.service.impl.CategoryServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Loggable
@WebServlet("/category")
public class CategoryServlet extends BaseServlet {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryServlet() {
        this(new CategoryServiceImpl());
    }

    public CategoryServlet(CategoryService categoryService) {
        this.categoryService = categoryService;
        this.categoryMapper = CategoryMapper.INSTANCE;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> optionalId = getId(req);
            if (optionalId.isEmpty()) {
                getAllCategories(resp);
                return;
            }
            long id = optionalId.get();
            getCategoryById(resp, id);
        } catch (EntityNotFoundException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, "Ошибка получения категорий: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void getAllCategories(HttpServletResponse resp) throws IOException {
        List<Category> categories = categoryService.getAll();
        List<CategoryResponseDto> responseDtoList = categoryMapper.toDto(categories);
        sendJson(resp, ApiResponse.success(responseDtoList));
    }

    private void getCategoryById(HttpServletResponse resp, Long id) throws IOException {
        Category category = categoryService.getById(id);
        CategoryResponseDto response = categoryMapper.toDto(category);
        sendJson(resp, ApiResponse.success(response));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CategoryRequestDto requestDto = parseJson(req, CategoryRequestDto.class);

            if (categoryService.existsByName(requestDto.name())) {
                sendError(resp, String.format("Категория %s уже имеется", requestDto.name()),
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Category category = categoryMapper.toEntity(requestDto);
            Category savedCategory = categoryService.save(category);
            CategoryResponseDto result = categoryMapper.toDto(savedCategory);

            sendJson(resp, ApiResponse.success(result), HttpServletResponse.SC_CREATED);

        } catch (AlreadyExistException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, "Ошибка создания категории: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID категории", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            CategoryRequestDto categoryDto = parseJson(req, CategoryRequestDto.class);
            Category entity = categoryMapper.toEntity(categoryDto);
            Category updated = categoryService.update(id.get(), entity);
            CategoryResponseDto result = categoryMapper.toDto(updated);
            sendJson(resp, ApiResponse.success(result));
        } catch (EntityNotFoundException | AlreadyExistException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, "Ошибка обновления категории: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID категории", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            boolean deleted = categoryService.deleteById(id.get());
            if (deleted) {
                sendJson(resp, ApiResponse.success("Категория удалена"));
            } else {
                sendError(resp, "Ошибка удаления категории",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            sendError(resp, "Ошибка удаления категории: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
