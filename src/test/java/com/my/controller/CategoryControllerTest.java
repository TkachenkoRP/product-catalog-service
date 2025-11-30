package com.my.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.my.dto.CategoryRequestDto;
import com.my.dto.CategoryResponseDto;
import com.my.dto.ErrorResponseDto;
import com.my.exception.AlreadyExistException;
import com.my.exception.EntityNotFoundException;
import com.my.mapper.CategoryMapper;
import com.my.model.Category;
import com.my.service.CategoryService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest extends AbstractControllerTest {
    @Mock
    private CategoryService categoryService;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        ExceptionHandlerController exceptionHandlerController = new ExceptionHandlerController();
        setUpMockMvc(categoryController, exceptionHandlerController);
    }

    @Test
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

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/category",
                HttpStatus.OK
        );

        List<CategoryResponseDto> result = fromResponse(response, new TypeReference<>() {
        });

        assertThat(result)
                .hasSize(2)
                .extracting(CategoryResponseDto::name)
                .containsExactly("Electronics", "Clothing");

        verify(categoryService).getAll();
    }

    @Test
    void whenGetCategoryById_thenReturnCategory() throws Exception {
        Long categoryId = 1L;
        Category category = new Category(categoryId, "Electronics");
        CategoryResponseDto responseDto = new CategoryResponseDto(categoryId, "Electronics");

        when(categoryService.getById(categoryId)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/category/" + categoryId,
                HttpStatus.OK
        );

        CategoryResponseDto result = fromResponse(response, CategoryResponseDto.class);

        assertThat(result)
                .extracting(CategoryResponseDto::id, CategoryResponseDto::name)
                .containsExactly(categoryId, "Electronics");

        verify(categoryService).getById(categoryId);
    }

    @Test
    void whenGetNonExistingCategoryById_thenReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        when(categoryService.getById(nonExistingId))
                .thenThrow(new EntityNotFoundException("Категория не найдена"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.GET,
                "/api/category/" + nonExistingId,
                HttpStatus.NOT_FOUND
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Категория не найдена");
    }

    @Test
    void whenCreateCategory_thenReturnCreatedCategory() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("Sports");
        Category categoryEntity = new Category("Sports");
        Category savedCategory = new Category(1L, "Sports");
        CategoryResponseDto responseDto = new CategoryResponseDto(1L, "Sports");

        when(categoryMapper.toEntity(requestDto)).thenReturn(categoryEntity);
        when(categoryService.save(categoryEntity)).thenReturn(savedCategory);
        when(categoryMapper.toDto(savedCategory)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/category",
                requestDto,
                HttpStatus.OK
        );

        CategoryResponseDto result = fromResponse(response, CategoryResponseDto.class);

        assertThat(result)
                .extracting(CategoryResponseDto::id, CategoryResponseDto::name)
                .containsExactly(1L, "Sports");

        verify(categoryService).save(categoryEntity);
    }

    @Test
    void whenCreateCategoryWithExistingName_thenReturnBadRequest() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("Electronics");
        Category categoryEntity = new Category("Electronics");

        when(categoryMapper.toEntity(requestDto)).thenReturn(categoryEntity);
        when(categoryService.save(categoryEntity))
                .thenThrow(new AlreadyExistException("Electronics уже существует"));

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/category",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Electronics уже существует");
    }

    @Test
    void whenCreateCategoryWithBlankName_thenReturnBadRequest() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/category",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Поле name должно быть заполнено");
    }

    @Test
    void whenCreateCategoryWithShortName_thenReturnBadRequest() throws Exception {
        CategoryRequestDto requestDto = new CategoryRequestDto("A");

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/category",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Название категории должно быть от 2 до 100 символов");
    }

    @Test
    void whenCreateCategoryWithLongName_thenReturnBadRequest() throws Exception {
        String longName = "A".repeat(101);
        CategoryRequestDto requestDto = new CategoryRequestDto(longName);

        MockHttpServletResponse response = performRequest(
                HttpMethod.POST,
                "/api/category",
                requestDto,
                HttpStatus.BAD_REQUEST
        );

        ErrorResponseDto error = fromResponse(response, ErrorResponseDto.class);
        assertThat(error.message()).contains("Название категории должно быть от 2 до 100 символов");
    }

    @Test
    void whenUpdateCategory_thenReturnUpdatedCategory() throws Exception {
        Long categoryId = 1L;
        CategoryRequestDto requestDto = new CategoryRequestDto("Updated Electronics");
        Category categoryEntity = new Category("Updated Electronics");
        Category updatedCategory = new Category(categoryId, "Updated Electronics");
        CategoryResponseDto responseDto = new CategoryResponseDto(categoryId, "Updated Electronics");

        when(categoryMapper.toEntity(requestDto)).thenReturn(categoryEntity);
        when(categoryService.update(eq(categoryId), eq(categoryEntity))).thenReturn(updatedCategory);
        when(categoryMapper.toDto(updatedCategory)).thenReturn(responseDto);

        MockHttpServletResponse response = performRequest(
                HttpMethod.PATCH,
                "/api/category/" + categoryId,
                requestDto,
                HttpStatus.OK
        );

        CategoryResponseDto result = fromResponse(response, CategoryResponseDto.class);

        assertThat(result)
                .extracting(CategoryResponseDto::id, CategoryResponseDto::name)
                .containsExactly(categoryId, "Updated Electronics");

        verify(categoryService).update(categoryId, categoryEntity);
    }

    @Test
    void whenDeleteCategorySuccessfully_thenReturnSuccess() throws Exception {
        Long categoryId = 1L;
        when(categoryService.deleteById(categoryId)).thenReturn(true);

        MockHttpServletResponse response = performRequest(
                HttpMethod.DELETE,
                "/api/category/" + categoryId,
                HttpStatus.OK
        );

        assertThat(response.getContentAsString()).isEmpty();
        verify(categoryService).deleteById(categoryId);
    }

    @Test
    void whenDeleteCategoryFails_thenStillReturnSuccess() throws Exception {
        Long categoryId = 1L;
        when(categoryService.deleteById(categoryId)).thenReturn(false);

        MockHttpServletResponse response = performRequest(
                HttpMethod.DELETE,
                "/api/category/" + categoryId,
                HttpStatus.OK
        );

        assertThat(response.getContentAsString()).isEmpty();
        verify(categoryService).deleteById(categoryId);
    }

}
