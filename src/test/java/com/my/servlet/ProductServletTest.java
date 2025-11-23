package com.my.servlet;

import com.my.model.Product;
import com.my.model.ProductFilter;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    void doGet_WithoutId_ReturnsAllProductsWithFilter() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        when(request.getParameter("categoryId")).thenReturn("1");
        when(request.getParameter("brandId")).thenReturn("2");
        when(request.getParameter("minPrice")).thenReturn("10.0");
        when(request.getParameter("maxPrice")).thenReturn("100.0");
        when(request.getParameter("minStock")).thenReturn("5");

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product1");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product2");

        List<Product> products = Arrays.asList(product1, product2);
        when(productService.getAll(any(ProductFilter.class))).thenReturn(products);

        productServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Product1")
                .contains("Product2");
    }

    @Test
    void doGet_WithId_ReturnsProductById() throws Exception {
        when(request.getParameter("id")).thenReturn("1");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(99.99);
        when(productService.getById(1L)).thenReturn(product);

        productServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Test Product");
    }

    @Test
    void doDelete_DeleteProduct_Success() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(productService.deleteById(1L)).thenReturn(true);

        productServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Товар удален");
    }
}
