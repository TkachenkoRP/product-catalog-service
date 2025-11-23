package com.my.servlet;

import com.my.annotation.Loggable;
import com.my.dto.ApiResponse;
import com.my.dto.BrandRequestDto;
import com.my.dto.BrandResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.ArgumentNotValidException;
import com.my.exception.EmptyBodyException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.service.BrandService;
import com.my.service.impl.BrandServiceImpl;
import com.my.validation.Validation;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Loggable
@WebServlet("/brand")
public class BrandServlet extends BaseServlet {
    private final BrandService brandService;
    private final BrandMapper brandMapper;

    public BrandServlet() {
        this(new BrandServiceImpl());
    }

    public BrandServlet(BrandService brandService) {
        this.brandService = brandService;
        this.brandMapper = BrandMapper.INSTANCE;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> optionalId = getId(req);
            if (optionalId.isEmpty()) {
                getAllBrands(resp);
                return;
            }
            long id = optionalId.get();
            getBrandById(resp, id);
        } catch (Exception e) {
            sendError(resp, "Ошибка получения брендов: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void getAllBrands(HttpServletResponse resp) throws IOException {
        List<Brand> brands = brandService.getAll();
        List<BrandResponseDto> responseDtoList = brandMapper.toDto(brands);
        sendJson(resp, ApiResponse.success(responseDtoList));
    }

    private void getBrandById(HttpServletResponse resp, Long id) throws IOException {
        Brand brand = brandService.getById(id);
        BrandResponseDto response = brandMapper.toDto(brand);
        sendJson(resp, ApiResponse.success(response));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BrandRequestDto requestDto = parseJson(req, BrandRequestDto.class);

            Validation.validateBrandCreate(requestDto);

            if (brandService.existsByName(requestDto.name())) {
                sendError(resp, String.format("Бренд %s уже имеется", requestDto.name()),
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Brand entity = brandMapper.toEntity(requestDto);
            Brand savedBrand = brandService.save(entity);
            BrandResponseDto result = brandMapper.toDto(savedBrand);

            sendJson(resp, ApiResponse.success(result), HttpServletResponse.SC_CREATED);

        } catch (ArgumentNotValidException e) {
            sendValidationError(resp, e.getMessage());
        } catch (AlreadyExistException | EmptyBodyException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, "Ошибка создания бренда: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID бренда", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            BrandRequestDto requestDto = parseJson(req, BrandRequestDto.class);

            Validation.validateBrandUpdate(requestDto);

            Brand entity = brandMapper.toEntity(requestDto);
            Brand updated = brandService.update(id.get(), entity);
            BrandResponseDto result = brandMapper.toDto(updated);
            sendJson(resp, ApiResponse.success(result));
        } catch (ArgumentNotValidException e) {
            sendValidationError(resp, e.getMessage());
        } catch (EntityNotFoundException | AlreadyExistException | EmptyBodyException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, "Ошибка обновления бренда: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> id = getId(req);
            if (id.isEmpty()) {
                sendError(resp, "Укажите ID бренда", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            boolean deleted = brandService.deleteById(id.get());
            if (deleted) {
                sendJson(resp, ApiResponse.success("Бренд удален"));
            } else {
                sendError(resp, "Ошибка удаления бренда",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            sendError(resp, "Ошибка удаления бренда: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

