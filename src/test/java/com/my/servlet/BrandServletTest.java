package com.my.servlet;

import com.my.model.Brand;
import com.my.service.BrandService;
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
class BrandServletTest {

    @Mock
    private BrandService brandService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private BrandServlet brandServlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        brandServlet = new BrandServlet(brandService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void doGet_WithoutId_ReturnsAllBrands() throws Exception {
        when(request.getParameter("id")).thenReturn(null);

        Brand brand1 = new Brand();
        brand1.setId(1L);
        brand1.setName("Brand1");

        Brand brand2 = new Brand();
        brand2.setId(2L);
        brand2.setName("Brand2");

        List<Brand> brands = Arrays.asList(brand1, brand2);
        when(brandService.getAll()).thenReturn(brands);

        brandServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Brand1")
                .contains("Brand2");
    }

    @Test
    void doGet_WithId_ReturnsBrandById() throws Exception {
        when(request.getParameter("id")).thenReturn("1");

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Test Brand");
        when(brandService.getById(1L)).thenReturn(brand);

        brandServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Test Brand");
    }


    @Test
    void doDelete_DeleteBrand_Success() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(brandService.deleteById(1L)).thenReturn(true);

        brandServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":true")
                .contains("Бренд удален");
    }

    @Test
    void doDelete_BrandNotFound_ReturnsError() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(brandService.deleteById(1L)).thenReturn(false);

        brandServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String jsonResponse = responseWriter.toString();
        assertThat(jsonResponse).contains("\"success\":false")
                .contains("Ошибка удаления бренда");
    }
}
