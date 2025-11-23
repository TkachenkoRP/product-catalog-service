package com.my.servlet;

import com.my.exception.EntityNotFoundException;
import com.my.model.Category;
import com.my.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServletTest {
    @Mock
    private CategoryService categoryService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private CategoryServlet categoryServlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        categoryServlet = new CategoryServlet(categoryService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void doGet_WithoutId_ReturnsAllCategories() throws Exception {
        when(request.getParameter("id")).thenReturn(null);

        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Category1");

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Category2");

        List<Category> categories = Arrays.asList(cat1, cat2);
        when(categoryService.getAll()).thenReturn(categories);

        categoryServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Category1")
                .contains("Category2");
    }

    @Test
    void doGet_WithId_ReturnsCategoryById() throws Exception {
        when(request.getParameter("id")).thenReturn("1");

        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        when(categoryService.getById(1L)).thenReturn(category);

        categoryServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Test Category");
    }

    @Test
    void doGet_CategoryNotFound_ReturnsError() throws Exception {
        when(request.getParameter("id")).thenReturn("999");
        when(categoryService.getById(999L)).thenThrow(new EntityNotFoundException("Category not found"));

        categoryServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false")
                .contains("Category not found");
    }
}
