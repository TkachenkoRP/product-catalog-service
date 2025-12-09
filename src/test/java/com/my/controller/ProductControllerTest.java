package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.dto.ApiResponseDto;
import com.my.dto.ProductRequestDto;
import com.my.dto.ProductResponseDto;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    @Autowired
    public ProductControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void whenGetAllProductsWithFilter_thenReturnFilteredProducts() throws Exception {
        List<Product> products = List.of(
                new Product(1L, "Product 1", 1L, 1L, 99.99, 10)
        );
        List<ProductResponseDto> responseDtos = List.of(
                new ProductResponseDto(1L, "Product 1", null, null, 99.99, 10)
        );

        when(productService.getAll(any(ProductFilter.class))).thenReturn(products);
        when(productMapper.toDto(products)).thenReturn(responseDtos);

        String response = mockMvc.perform(get("/api/product")
                        .param("categoryId", "1")
                        .param("minPrice", "50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<ProductResponseDto>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).hasSize(1);
        verify(productService).getAll(any(ProductFilter.class));
    }

    @Test
    void whenGetProductById_thenReturnProduct() throws Exception {
        Long productId = 1L;
        Product product = new Product(productId, "Product 1", 1L, 1L, 99.99, 10);
        ProductResponseDto responseDto = new ProductResponseDto(productId, "Product 1", null, null, 99.99, 10);

        when(productService.getById(productId)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        String response = mockMvc.perform(get("/api/product/" + productId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<ProductResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(
                        ProductResponseDto::id,
                        ProductResponseDto::name,
                        ProductResponseDto::category,
                        ProductResponseDto::brand,
                        ProductResponseDto::price,
                        ProductResponseDto::stock
                )
                .containsExactly(productId, "Product 1", null, null, 99.99, 10);

        verify(productService).getById(productId);
    }

    @Test
    void whenGetNonExistingProductById_thenReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        when(productService.getById(nonExistingId))
                .thenThrow(new EntityNotFoundException("Товар не найден"));

        String response = mockMvc.perform(get("/api/product/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<ProductResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Товар не найден");
    }

    @Test
    void whenCreateProduct_thenReturnCreatedProduct() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("New Product", 1L, 1L, 99.99, 10);
        Product productEntity = new Product("New Product", 1L, 1L, 99.99, 10);
        Product savedProduct = new Product(1L, "New Product", 1L, 1L, 99.99, 10);
        ProductResponseDto responseDto = new ProductResponseDto(1L, "New Product", null, null, 99.99, 10);

        when(productMapper.toEntity(requestDto)).thenReturn(productEntity);
        when(productService.save(productEntity)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<ProductResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(ProductResponseDto::name)
                .isEqualTo("New Product");

        verify(productService).save(productEntity);
    }

    @Test
    void whenCreateProductWithBlankName_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("", 1L, 1L, 99.99, 10);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле name должно быть заполнено");
    }

    @Test
    void whenCreateProductWithShortName_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("A", 1L, 1L, 99.99, 10);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Название товара должно быть от 2 до 200 символов");
    }

    @Test
    void whenCreateProductWithNullCategoryId_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", null, 1L, 99.99, 10);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле categoryId должно быть заполнено");
    }

    @Test
    void whenCreateProductWithNegativeCategoryId_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", -1L, 1L, 99.99, 10);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("ID категории должен быть положительным числом");
    }

    @Test
    void whenCreateProductWithNullBrandId_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", 1L, null, 99.99, 10);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле brandId должно быть заполнено");
    }

    @Test
    void whenCreateProductWithNegativePrice_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", 1L, 1L, -10.0, 10);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Цена должна быть положительным числом");
    }

    @Test
    void whenCreateProductWithNullStock_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", 1L, 1L, 99.99, null);

        String response = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Поле stock должно быть заполнено");
    }

    @Test
    void whenUpdateProductWithInvalidData_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("A", -1L, -1L, -10.0, -5);

        String response = mockMvc.perform(patch("/api/product/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .contains("Название товара должно быть от 2 до 200 символов")
                .contains("ID категории должен быть положительным числом")
                .contains("ID бренда должен быть положительным числом")
                .contains("Цена должна быть положительным числом")
                .contains("Количество на складе должно быть положительным числом");
    }

    @Test
    void whenUpdateProduct_thenReturnUpdatedProduct() throws Exception {
        Long productId = 1L;
        ProductRequestDto requestDto = new ProductRequestDto("Updated Product", 2L, 2L, 199.99, 15);
        Product productEntity = new Product("Updated Product", 2L, 2L, 199.99, 15);
        Product updatedProduct = new Product(productId, "Updated Product", 2L, 2L, 199.99, 15);
        ProductResponseDto responseDto = new ProductResponseDto(productId, "Updated Product", null, null, 199.99, 15);

        when(productMapper.toEntity(requestDto)).thenReturn(productEntity);
        when(productService.update(eq(productId), eq(productEntity))).thenReturn(updatedProduct);
        when(productMapper.toDto(updatedProduct)).thenReturn(responseDto);

        String response = mockMvc.perform(patch("/api/product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<ProductResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(ProductResponseDto::name)
                .isEqualTo("Updated Product");

        verify(productService).update(productId, productEntity);
    }

    @Test
    void whenDeleteProductSuccessfully_thenReturnSuccess() throws Exception {
        Long productId = 1L;
        when(productService.deleteById(productId)).thenReturn(true);

        String response = mockMvc.perform(delete("/api/product/" + productId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<Void> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Товар успешно удален");
        verify(productService).deleteById(productId);
    }
}
