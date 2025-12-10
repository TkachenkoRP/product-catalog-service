package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.dto.ApiResponseDto;
import com.my.dto.BrandRequestDto;
import com.my.dto.BrandResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.service.BrandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrandController.class)
@DisplayName("Тесты контроллера брендов")
class BrandControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private BrandService brandService;

    @MockBean
    private BrandMapper brandMapper;

    @Autowired
    public BrandControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("GET /api/brand - Получение всех брендов")
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

        String response = mockMvc.perform(get("/api/brand"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<BrandResponseDto>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .hasSize(2)
                .extracting(BrandResponseDto::name)
                .containsExactly("Samsung", "Apple");

        verify(brandService).getAll();
    }

    @Test
    @DisplayName("GET /api/brand/{id} - Получение бренда по ID")
    void whenGetBrandById_thenReturnBrand() throws Exception {
        Long brandId = 1L;
        Brand brand = new Brand(brandId, "Samsung");
        BrandResponseDto responseDto = new BrandResponseDto(brandId, "Samsung");

        when(brandService.getById(brandId)).thenReturn(brand);
        when(brandMapper.toDto(brand)).thenReturn(responseDto);

        String response = mockMvc.perform(get("/api/brand/" + brandId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<BrandResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(BrandResponseDto::id, BrandResponseDto::name)
                .containsExactly(brandId, "Samsung");

        verify(brandService).getById(brandId);
    }

    @Test
    @DisplayName("GET /api/brand/{id} - Несуществующий бренд возвращает 404")
    void whenGetNonExistingBrandById_thenReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        when(brandService.getById(nonExistingId))
                .thenThrow(new EntityNotFoundException("Бренд не найден"));

        String response = mockMvc.perform(get("/api/brand/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Бренд не найден");
    }

    @Test
    @DisplayName("POST /api/brand - Создание нового бренда")
    void whenCreateBrand_thenReturnCreatedBrand() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("Sony");
        Brand brandEntity = new Brand("Sony");
        Brand savedBrand = new Brand(1L, "Sony");
        BrandResponseDto responseDto = new BrandResponseDto(1L, "Sony");

        when(brandMapper.toEntity(requestDto)).thenReturn(brandEntity);
        when(brandService.save(brandEntity)).thenReturn(savedBrand);
        when(brandMapper.toDto(savedBrand)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/api/brand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<BrandResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(BrandResponseDto::id, BrandResponseDto::name)
                .containsExactly(1L, "Sony");

        verify(brandService).save(brandEntity);
    }

    @Test
    @DisplayName("POST /api/brand - Попытка создания бренда с существующим именем")
    void whenCreateBrandWithExistingName_thenReturnBadRequest() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("Samsung");
        Brand brandEntity = new Brand("Samsung");

        when(brandMapper.toEntity(requestDto)).thenReturn(brandEntity);
        when(brandService.save(brandEntity))
                .thenThrow(new AlreadyExistException("Samsung уже существует"));

        String response = mockMvc.perform(post("/api/brand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<BrandResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Samsung уже существует");
    }

    @Test
    @DisplayName("POST /api/brand - Валидация: пустое название бренда")
    void whenCreateBrandWithBlankName_thenReturnBadRequest() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("");

        String response = mockMvc.perform(post("/api/brand")
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
    @DisplayName("POST /api/brand - Валидация: короткое название бренда")
    void whenCreateBrandWithShortName_thenReturnBadRequest() throws Exception {
        BrandRequestDto requestDto = new BrandRequestDto("A");

        String response = mockMvc.perform(post("/api/brand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Название бренда должно быть от 2 до 100 символов");
    }

    @Test
    @DisplayName("POST /api/brand - Валидация: длинное название бренда")
    void whenCreateBrandWithLongName_thenReturnBadRequest() throws Exception {
        String longName = "A".repeat(101);
        BrandRequestDto requestDto = new BrandRequestDto(longName);

        String response = mockMvc.perform(post("/api/brand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Название бренда должно быть от 2 до 100 символов");
    }

    @Test
    @DisplayName("PATCH /api/brand/{id} - Обновление бренда")
    void whenUpdateBrand_thenReturnUpdatedBrand() throws Exception {
        Long brandId = 1L;
        BrandRequestDto requestDto = new BrandRequestDto("Updated Samsung");
        Brand brandEntity = new Brand("Updated Samsung");
        Brand updatedBrand = new Brand(brandId, "Updated Samsung");
        BrandResponseDto responseDto = new BrandResponseDto(brandId, "Updated Samsung");

        when(brandMapper.toEntity(requestDto)).thenReturn(brandEntity);
        when(brandService.update(brandId, brandEntity)).thenReturn(updatedBrand);
        when(brandMapper.toDto(updatedBrand)).thenReturn(responseDto);

        String response = mockMvc.perform(patch("/api/brand/" + brandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<BrandResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(BrandResponseDto::id, BrandResponseDto::name)
                .containsExactly(brandId, "Updated Samsung");

        verify(brandService).update(brandId, brandEntity);
    }

    @Test
    @DisplayName("DELETE /api/brand/{id} - Успешное удаление бренда")
    void whenDeleteBrand_thenReturnSuccess() throws Exception {
        Long brandId = 1L;
        when(brandService.deleteById(brandId)).thenReturn(true);

        String response = mockMvc.perform(delete("/api/brand/" + brandId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<Void> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Бренд успешно удален");
        verify(brandService).deleteById(brandId);
    }
}
