package com.my.mapper;

import com.my.dto.ProductDto;
import com.my.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    Product copyProduct(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateProduct(Product sourceProduct, @MappingTarget Product targetProduct);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    ProductDto toDto(Product product);
}
