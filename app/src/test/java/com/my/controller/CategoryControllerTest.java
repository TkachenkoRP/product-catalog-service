package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.dto.ApiResponseDto;
import com.my.dto.CategoryRequestDto;
import com.my.dto.CategoryResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.service.CategoryService;
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

@WebMvcTest(CategoryController.class)
@DisplayName("Тесты контроллера категорий")
class CategoryControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryMapper categoryMapper;

    @Autowired
    public CategoryControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("GET /api/category - Получение всех категорий")
    void whenGetAllCategories_thenReturnCategoryList() throws Exception {
        List<Category> categories = Arrays.asList(
                new Category(1L, "Electronics"),
                new Category(2L, "Clothing")
        );
        List<CategoryResponseDto> responseDtos = Arrays.asList(
                new CategoryResponseDto(1L, "Electronics"),
                new CategoryResponseDto(2L, "Clothing")
        );

        when(categoryService.getAll()).thenReturn(categories);
        when(categoryMapper.toDto(categories)).thenReturn(responseDtos);

        String response = mockMvc.perform(get("/api/category"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<CategoryResponseDto>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .hasSize(2)
                .extracting(CategoryResponseDto::name)
                .containsExactly("Electronics", "Clothing");

        verify(categoryService).getAll();
    }

    @Test
    @DisplayName("GET /api/category/{id} - Получение категории по ID")
    void whenGetCategoryById_thenReturnCategory() throws Exception {
        Long categoryId = 1L;
        Category category = new Category(categoryId, "Electronics");
        CategoryResponseDto responseDto = new CategoryResponseDto(categoryId, "Electronics");

        when(categoryService.getById(categoryId)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(responseDto);

        String response = mockMvc.perform(get("/api/category/" + categoryId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<CategoryResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(CategoryResponseDto::id, CategoryResponseDto::name)
                .containsExactly(categoryId, "Electronics");

        verify(categoryService).getById(categoryId);
    }

    @Test
    @DisplayName("GET /api/category/{id} - Несуществующая категория возвращает 404")
    void whenGetNonExistingCategoryById_thenReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        when(categoryService.getById(nonExistingId))
                .thenThrow(new EntityNotFoundException("Категория не найдена"));

        String response = mockMvc.perform(get("/api/category/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Категория не найдена");
    }

    @Test
    @DisplayName("POST /api/category - Создание новой категории")
    void whenCreateCategory_thenReturnCreatedCategory() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("Sports");
        Category categoryEntity = new Category("Sports");
        Category savedCategory = new Category(1L, "Sports");
        CategoryResponseDto responseDto = new CategoryResponseDto(1L, "Sports");

        when(categoryMapper.toEntity(requestDto)).thenReturn(categoryEntity);
        when(categoryService.save(categoryEntity)).thenReturn(savedCategory);
        when(categoryMapper.toDto(savedCategory)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<CategoryResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(CategoryResponseDto::id, CategoryResponseDto::name)
                .containsExactly(1L, "Sports");

        verify(categoryService).save(categoryEntity);
    }

    @Test
    @DisplayName("POST /api/category - Попытка создания категории с существующим именем")
    void whenCreateCategoryWithExistingName_thenReturnBadRequest() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("Electronics");
        Category categoryEntity = new Category("Electronics");

        when(categoryMapper.toEntity(requestDto)).thenReturn(categoryEntity);
        when(categoryService.save(categoryEntity))
                .thenThrow(new AlreadyExistException("Electronics уже существует"));

        String response = mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<CategoryResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Electronics уже существует");
    }

    @Test
    @DisplayName("POST /api/category - Валидация: пустое название категории")
    void whenCreateCategoryWithBlankName_thenReturnBadRequest() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("");

        String response = mockMvc.perform(post("/api/category")
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
    @DisplayName("POST /api/category - Валидация: короткое название категории")
    void whenCreateCategoryWithShortName_thenReturnBadRequest() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("A");

        String response = mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Название категории должно быть от 2 до 100 символов");
    }

    @Test
    @DisplayName("POST /api/category - Валидация: длинное название категории")
    void whenCreateCategoryWithLongName_thenReturnBadRequest() throws Exception {
        String longName = "A".repeat(101);
        CategoryRequestDto requestDto = new CategoryRequestDto(longName);

        String response = mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<List<String>> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data()).contains("Название категории должно быть от 2 до 100 символов");
    }

    @Test
    @DisplayName("PATCH /api/category/{id} - Обновление категории")
    void whenUpdateCategory_thenReturnUpdatedCategory() throws Exception {
        Long categoryId = 1L;
        CategoryRequestDto requestDto = new CategoryRequestDto("Updated Electronics");
        Category categoryEntity = new Category("Updated Electronics");
        Category updatedCategory = new Category(categoryId, "Updated Electronics");
        CategoryResponseDto responseDto = new CategoryResponseDto(categoryId, "Updated Electronics");

        when(categoryMapper.toEntity(requestDto)).thenReturn(categoryEntity);
        when(categoryService.update(categoryId, categoryEntity)).thenReturn(updatedCategory);
        when(categoryMapper.toDto(updatedCategory)).thenReturn(responseDto);

        String response = mockMvc.perform(patch("/api/category/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<CategoryResponseDto> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.data())
                .extracting(CategoryResponseDto::id, CategoryResponseDto::name)
                .containsExactly(categoryId, "Updated Electronics");

        verify(categoryService).update(categoryId, categoryEntity);
    }

    @Test
    @DisplayName("DELETE /api/category/{id} - Успешное удаление категории")
    void whenDeleteCategorySuccessfully_thenReturnSuccess() throws Exception {
        Long categoryId = 1L;
        when(categoryService.deleteById(categoryId)).thenReturn(true);

        String response = mockMvc.perform(delete("/api/category/" + categoryId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponseDto<Void> actualResponse = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(actualResponse.message()).contains("Категория успешно удалена");
        verify(categoryService).deleteById(categoryId);
    }
}
