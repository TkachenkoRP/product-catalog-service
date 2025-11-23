package com.my.servlet;

import com.my.annotation.Loggable;
import com.my.dto.ApiResponse;
import com.my.dto.ProductRequestDto;
import com.my.dto.ProductResponseDto;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.ProductMapper;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.service.ProductService;
import com.my.service.impl.ProductServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Loggable
@WebServlet("/product")
public class ProductServlet extends BaseServlet {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductServlet() {
        this(new ProductServiceImpl());
    }

    public ProductServlet(ProductService productService) {
        this.productService = productService;
        this.productMapper = ProductMapper.INSTANCE;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> optionalId = getId(req);
            if (optionalId.isEmpty()) {
                ProductFilter filter = getProductFilter(req);
                getAllProducts(resp, filter);
                return;
            }
            long id = optionalId.get();
            getProductById(resp, id);
        } catch (Exception e) {
            sendError(resp, "Ошибка получения товара: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private ProductFilter getProductFilter(HttpServletRequest req) {
        Long categoryId = getLongParameter(req, ProductFilter.Fields.categoryId);
        Long brandId = getLongParameter(req, ProductFilter.Fields.brandId);
        Double minPrice = getDoubleParameter(req, ProductFilter.Fields.minPrice);
        Double maxPrice = getDoubleParameter(req, ProductFilter.Fields.maxPrice);
        Integer minStock = getIntegerParameter(req, ProductFilter.Fields.minStock);

        return new ProductFilter(categoryId, brandId, minPrice, maxPrice, minStock);
    }

    private void getAllProducts(HttpServletResponse resp, ProductFilter filter) throws IOException {
        List<Product> products = productService.getAll(filter);
        List<ProductResponseDto> responseDtoList = productMapper.toDto(products);
        sendJson(resp, ApiResponse.success(responseDtoList));
    }

    private void getProductById(HttpServletResponse resp, Long id) throws IOException {
        Product product = productService.getById(id);
        ProductResponseDto response = productMapper.toDto(product);
        sendJson(resp, ApiResponse.success(response));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ProductRequestDto requestDto = parseJson(req, ProductRequestDto.class);
            Product entity = productMapper.toEntity(requestDto);
            Product saved = productService.save(entity);
            ProductResponseDto result = productMapper.toDto(saved);

            sendJson(resp, ApiResponse.success(result), HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            sendError(resp, "Ошибка создания товара: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID товара", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            ProductRequestDto requestDto = parseJson(req, ProductRequestDto.class);
            Product entity = productMapper.toEntity(requestDto);
            Product updated = productService.update(id.get(), entity);
            ProductResponseDto result = productMapper.toDto(updated);
            sendJson(resp, ApiResponse.success(result));
        } catch (EntityNotFoundException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, "Ошибка обновления товара: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID товара", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            boolean deleted = productService.deleteById(id.get());
            if (deleted) {
                sendJson(resp, ApiResponse.success("Товар удален"));
            } else {
                sendError(resp, "Ошибка удаления товара",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            sendError(resp, "Ошибка удаления товара: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
