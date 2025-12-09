package com.my.mapper;

import com.my.dto.BrandRequestDto;
import com.my.dto.BrandResponseDto;
import com.my.model.Brand;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Маппер для преобразования между сущностью Brand и соответствующими DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    /**
     * Обновляет поля целевой сущности Brand значениями из исходной сущности.
     * Игнорирует нулевые значения в исходной сущности.
     *
     * @param sourceBrand исходная сущность Brand с новыми значениями
     * @param targetBrand целевая сущность Brand для обновления
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateBrand(Brand sourceBrand, @MappingTarget Brand targetBrand);

    /**
     * Преобразует DTO запроса в сущность Brand.
     *
     * @param request DTO с данными бренда
     * @return сущность Brand
     */
    Brand toEntity(BrandRequestDto request);

    /**
     * Преобразует сущность Brand в DTO ответа.
     *
     * @param entity сущность Brand
     * @return DTO с данными бренда
     */
    BrandResponseDto toDto(Brand entity);

    /**
     * Преобразует список сущностей Brand в список DTO ответов.
     *
     * @param entities список сущностей Brand
     * @return список DTO с данными брендов
     */
    List<BrandResponseDto> toDto(List<Brand> entities);
}
