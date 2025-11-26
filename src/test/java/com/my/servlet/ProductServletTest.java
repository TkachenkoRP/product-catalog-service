package com.my.servlet;

import com.my.exception.EntityNotFoundException;
import com.my.service.ProductService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServletTest {
    @Mock
    private ProductService productService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private ProductServlet productServlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        productServlet = new ProductServlet(productService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void doPut_WithoutId_ReturnsError() throws Exception {
        when(request.getParameter("id")).thenReturn(null);

        productServlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false")
                .contains("Укажите ID товара");
    }

    @Test
    void doDelete_ValidId_DeletesProduct() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(productService.deleteById(1L)).thenReturn(true);

        productServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Товар удален");
    }

    @Test
    void doDelete_WithoutId_ReturnsError() throws Exception {
        when(request.getParameter("id")).thenReturn(null);

        productServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false")
                .contains("Укажите ID товара");
    }

    @Test
    void doDelete_DeleteFails_ReturnsError() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(productService.deleteById(1L)).thenReturn(false);

        productServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false")
                .contains("Ошибка удаления товара");
    }

    @Test
    void doGet_ProductNotFound_ReturnsError() throws Exception {
        when(request.getParameter("id")).thenReturn("999");
        when(productService.getById(999L))
                .thenThrow(new EntityNotFoundException("Товар не найден"));

        productServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false")
                .contains("Товар не найден");
    }
}
