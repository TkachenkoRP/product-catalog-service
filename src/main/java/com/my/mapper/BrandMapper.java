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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateBrand(Brand sourceBrand, @MappingTarget Brand targetBrand);

    Brand toEntity(BrandRequestDto request);

    BrandResponseDto toDto(Brand entity);

    List<BrandResponseDto> toDto(List<Brand> entities);
}
