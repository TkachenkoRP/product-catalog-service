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
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CategoryMap.class, CategoryMapper.class, BrandMap.class, BrandMapper.class})
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    Product copyProduct(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateProduct(Product sourceProduct, @MappingTarget Product targetProduct);

    Product toEntity(ProductRequestDto request);

    @Mapping(target = "category", source = "categoryId")
    @Mapping(target = "brand", source = "brandId")
    ProductResponseDto toDto(Product entity);

    List<ProductResponseDto> toDto(List<Product> entities);
}
