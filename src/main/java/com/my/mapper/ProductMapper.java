package com.my.mapper;

import com.my.dto.ProductRequestDto;
import com.my.dto.ProductResponseDto;
import com.my.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Маппер для преобразования между сущностью Product и соответствующими DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CategoryMap.class, CategoryMapper.class, BrandMap.class, BrandMapper.class})
public interface ProductMapper {
    /**
     * Обновляет поля целевой сущности Product значениями из исходной сущности.
     * Игнорирует нулевые значения в исходной сущности.
     *
     * @param sourceProduct исходная сущность Product с новыми значениями
     * @param targetProduct целевая сущность Product для обновления
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateProduct(Product sourceProduct, @MappingTarget Product targetProduct);

    /**
     * Преобразует DTO запроса в сущность Product.
     *
     * @param request DTO с данными товара
     * @return сущность Product
     */
    Product toEntity(ProductRequestDto request);

    /**
     * Преобразует сущность Product в DTO ответа.
     *
     * @param entity сущность Product
     * @return DTO с данными товара
     */
    @Mapping(target = "category", source = "categoryId")
    @Mapping(target = "brand", source = "brandId")
    ProductResponseDto toDto(Product entity);

    /**
     * Преобразует список сущностей Product в список DTO ответов.
     *
     * @param entities список сущностей Product
     * @return список DTO с данными товаров
     */
    List<ProductResponseDto> toDto(List<Product> entities);
}
