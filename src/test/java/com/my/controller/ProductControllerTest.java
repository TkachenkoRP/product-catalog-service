package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.my.dto.ErrorResponseDto;
import com.my.dto.ProductRequestDto;
import com.my.dto.ProductResponseDto;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest extends AbstractControllerTest {
    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        ExceptionHandlerController exceptionHandlerController = new ExceptionHandlerController();
        setUpMockMvc(productController, exceptionHandlerController);
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

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/product?categoryId=1&minPrice=50",
                HttpStatus.OK
        );

        List<ProductResponseDto> result = fromResponse(response, new TypeReference<>() {
        });
        assertThat(result).hasSize(1);
        verify(productService).getAll(any(ProductFilter.class));
    }

    @Test
    void whenGetProductById_thenReturnProduct() throws Exception {
        Long productId = 1L;
        Product product = new Product(productId, "Product 1", 1L, 1L, 99.99, 10);
        ProductResponseDto responseDto = new ProductResponseDto(productId, "Product 1", null, null, 99.99, 10);

        when(productService.getById(productId)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/product/" + productId,
                HttpStatus.OK
        );

        ProductResponseDto result = fromResponse(response, ProductResponseDto.class);

        assertThat(result)
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

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/product/" + nonExistingId,
                HttpStatus.NOT_FOUND
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Товар не найден");
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

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.OK
        );

        ProductResponseDto result = fromResponse(response, ProductResponseDto.class);

        assertThat(result)
                .extracting(ProductResponseDto::name)
                .isEqualTo("New Product");

        verify(productService).save(productEntity);
    }

    @Test
    void whenCreateProductWithBlankName_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("", 1L, 1L, 99.99, 10);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле name должно быть заполнено");
    }

    @Test
    void whenCreateProductWithShortName_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("A", 1L, 1L, 99.99, 10);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Название товара должно быть от 2 до 200 символов");
    }

    @Test
    void whenCreateProductWithNullCategoryId_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", null, 1L, 99.99, 10);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле categoryId должно быть заполнено");
    }

    @Test
    void whenCreateProductWithNegativeCategoryId_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", -1L, 1L, 99.99, 10);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("ID категории должен быть положительным числом");
    }

    @Test
    void whenCreateProductWithNullBrandId_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", 1L, null, 99.99, 10);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле brandId должно быть заполнено");
    }

    @Test
    void whenCreateProductWithNegativePrice_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", 1L, 1L, -10.0, 10);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Цена должна быть положительным числом");
    }

    @Test
    void whenCreateProductWithNullStock_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("Valid Name", 1L, 1L, 99.99, null);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/product",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле stock должно быть заполнено");
    }

    @Test
    void whenUpdateProductWithInvalidData_thenReturnBadRequest() throws Exception {
        ProductRequestDto requestDto = new ProductRequestDto("A", -1L, -1L, -10.0, -5);

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/product/1",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message())
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

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/product/" + productId,
                requestDto,
                HttpStatus.OK
        );

        ProductResponseDto result = fromResponse(response, ProductResponseDto.class);

        assertThat(result)
                .extracting(ProductResponseDto::name)
                .isEqualTo("Updated Product");

        verify(productService).update(productId, productEntity);
    }

    @Test
    void whenDeleteProductSuccessfully_thenReturnSuccess() throws Exception {
        Long productId = 1L;
        when(productService.deleteById(productId)).thenReturn(true);

        MockHttpServletResponse response = performRequest(
                HttpMethod.DELETE,
                "/api/product/" + productId,
                HttpStatus.OK
        );

        assertThat(response.getContentAsString()).isEmpty();
        verify(productService).deleteById(productId);
    }

    @Test
    void whenDeleteProductFails_thenStillReturnSuccess() throws Exception {
        Long productId = 1L;
        when(productService.deleteById(productId)).thenReturn(false);

        MockHttpServletResponse response = performRequest(
                HttpMethod.DELETE,
                "/api/product/" + productId,
                HttpStatus.OK
        );

        assertThat(response.getContentAsString()).isEmpty();
        verify(productService).deleteById(productId);
    }
}
