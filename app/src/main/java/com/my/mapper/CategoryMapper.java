package com.my.mapper;

import com.my.dto.CategoryRequestDto;
import com.my.dto.CategoryResponseDto;
import com.my.model.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Маппер для преобразования между сущностью Category и соответствующими DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    /**
     * Обновляет поля целевой сущности Category значениями из исходной сущности.
     * Игнорирует нулевые значения в исходной сущности.
     *
     * @param sourceCategory исходная сущность Category с новыми значениями
     * @param targetCategory целевая сущность Category для обновления
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateCategory(Category sourceCategory, @MappingTarget Category targetCategory);

    /**
     * Преобразует DTO запроса в сущность Category.
     *
     * @param request DTO с данными для создания категории
     * @return сущность Category
     */
    Category toEntity(CategoryRequestDto request);

    /**
     * Преобразует сущность Category в DTO ответа.
     *
     * @param entity сущность Category
     * @return DTO с данными категории
     */
    CategoryResponseDto toDto(Category entity);

    /**
     * Преобразует список сущностей Category в список DTO ответов.
     *
     * @param entities список сущностей Category
     * @return список DTO с данными категорий
     */
    List<CategoryResponseDto> toDto(List<Category> entities);
}
