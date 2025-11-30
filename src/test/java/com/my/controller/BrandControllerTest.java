package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.my.dto.BrandRequestDto;
import com.my.dto.BrandResponseDto;
import com.my.dto.ErrorResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.service.BrandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandControllerTest extends AbstractControllerTest {
    @Mock
    private BrandService brandService;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandController brandController;

    @BeforeEach
    void setUp() {
        ExceptionHandlerController exceptionHandlerController = new ExceptionHandlerController();
        setUpMockMvc(brandController, exceptionHandlerController);
    }

    @Test
    void whenGetAllBrands_thenReturnBrandList() throws Exception {
        List<Brand> brands = Arrays.asList(
                new Brand(1L, "Samsung"),
                new Brand(2L, "Apple")
        );
        List<BrandResponseDto> responseDtos = Arrays.asList(
                new BrandResponseDto(1L, "Samsung"),
                new BrandResponseDto(2L, "Apple")
        );

        when(brandService.getAll()).thenReturn(brands);
        when(brandMapper.toDto(brands)).thenReturn(responseDtos);

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/brand",
                HttpStatus.OK
        );

        List<BrandResponseDto> result = fromResponse(response, new TypeReference<>() {
        });

        assertThat(result)
                .hasSize(2)
                .extracting(BrandResponseDto::name)
                .containsExactly("Samsung", "Apple");

        verify(brandService).getAll();
    }

    @Test
    void whenGetBrandById_thenReturnBrand() throws Exception {
        Long brandId = 1L;
        Brand brand = new Brand(brandId, "Samsung");
        BrandResponseDto responseDto = new BrandResponseDto(brandId, "Samsung");

        when(brandService.getById(brandId)).thenReturn(brand);
        when(brandMapper.toDto(brand)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/brand/" + brandId,
                HttpStatus.OK
        );

        BrandResponseDto result = fromResponse(response, BrandResponseDto.class);

        assertThat(result)
                .extracting(BrandResponseDto::id, BrandResponseDto::name)
                .containsExactly(brandId, "Samsung");

        verify(brandService).getById(brandId);
    }

    @Test
    void whenGetNonExistingBrandById_thenReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        when(brandService.getById(nonExistingId))
                .thenThrow(new EntityNotFoundException("Бренд не найден"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/brand/" + nonExistingId,
                HttpStatus.NOT_FOUND
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Бренд не найден");
    }

    @Test
    void whenCreateBrand_thenReturnCreatedBrand() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("Sony");
        Brand brandEntity = new Brand("Sony");
        Brand savedBrand = new Brand(1L, "Sony");
        BrandResponseDto responseDto = new BrandResponseDto(1L, "Sony");

        when(brandMapper.toEntity(requestDto)).thenReturn(brandEntity);
        when(brandService.save(brandEntity)).thenReturn(savedBrand);
        when(brandMapper.toDto(savedBrand)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/brand",
                requestDto,
                HttpStatus.OK
        );

        BrandResponseDto result = fromResponse(response, BrandResponseDto.class);

        assertThat(result)
                .extracting(BrandResponseDto::id, BrandResponseDto::name)
                .containsExactly(1L, "Sony");

        verify(brandService).save(brandEntity);
    }

    @Test
    void whenCreateBrandWithExistingName_thenReturnBadRequest() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("Samsung");
        Brand brandEntity = new Brand("Samsung");

        when(brandMapper.toEntity(requestDto)).thenReturn(brandEntity);
        when(brandService.save(brandEntity))
                .thenThrow(new AlreadyExistException("Samsung уже существует"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/brand",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Samsung уже существует");
    }

    @Test
    void whenCreateBrandWithBlankName_thenReturnBadRequest() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/brand",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле name должно быть заполнено");
    }

    @Test
    void whenCreateBrandWithShortName_thenReturnBadRequest() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("A");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/brand",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Название бренда должно быть от 2 до 100 символов");
    }

    @Test
    void whenCreateBrandWithLongName_thenReturnBadRequest() throws Exception {
        String longName = "A".repeat(101);
        BrandRequestDto requestDto = new BrandRequestDto(longName);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/brand",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Название бренда должно быть от 2 до 100 символов");
    }

    @Test
    void whenUpdateBrand_thenReturnUpdatedBrand() throws Exception {
        Long brandId = 1L;
        BrandRequestDto requestDto = new BrandRequestDto("Updated Samsung");
        Brand brandEntity = new Brand("Updated Samsung");
        Brand updatedBrand = new Brand(brandId, "Updated Samsung");
        BrandResponseDto responseDto = new BrandResponseDto(brandId, "Updated Samsung");

        when(brandMapper.toEntity(requestDto)).thenReturn(brandEntity);
        when(brandService.update(eq(brandId), eq(brandEntity))).thenReturn(updatedBrand);
        when(brandMapper.toDto(updatedBrand)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/brand/" + brandId,
                requestDto,
                HttpStatus.OK
        );

        BrandResponseDto result = fromResponse(response, BrandResponseDto.class);

        assertThat(result)
                .extracting(BrandResponseDto::id, BrandResponseDto::name)
                .containsExactly(brandId, "Updated Samsung");

        verify(brandService).update(brandId, brandEntity);
    }

    @Test
    void whenDeleteBrand_thenReturnSuccess() throws Exception {
        Long brandId = 1L;
        when(brandService.deleteById(brandId)).thenReturn(true);

        MockHttpServletResponse response = performRequest(
                HttpMethod.DELETE,
                "/api/brand/" + brandId,
                HttpStatus.OK
        );

        assertThat(response.getContentAsString()).isEmpty();
        verify(brandService).deleteById(brandId);
    }

    @Test
    void whenDeleteBrandFails_thenStillReturnSuccess() throws Exception {
        Long brandId = 1L;
        when(brandService.deleteById(brandId)).thenReturn(false);

        MockHttpServletResponse response = performRequest(
                HttpMethod.DELETE,
                "/api/brand/" + brandId,
                HttpStatus.OK
        );

        assertThat(response.getContentAsString()).isEmpty();
        verify(brandService).deleteById(brandId);
    }

    @Test
    void whenGetBrandsWithQueryParameters_thenReturnFilteredResults() throws Exception {
        List<Brand> brands = List.of(new Brand(1L, "Samsung"));
        List<BrandResponseDto> responseDtos = List.of(new BrandResponseDto(1L, "Samsung"));

        when(brandService.getAll()).thenReturn(brands);
        when(brandMapper.toDto(brands)).thenReturn(responseDtos);

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/brand",
                HttpStatus.OK,
                Map.of("search", "sam", "page", "1")
        );

        List<BrandResponseDto> result = fromResponse(response, new TypeReference<>() {
        });
        assertThat(result).hasSize(1);
    }
}
