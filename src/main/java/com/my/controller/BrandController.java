package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.BrandRequestDto;
import com.my.dto.BrandResponseDto;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.service.BrandService;
import com.my.validation.ValidationGroups;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
@Audition
public class BrandController {
    private final BrandService brandService;
    private final BrandMapper brandMapper;

    @GetMapping
    public List<BrandResponseDto> getAll() {
        List<Brand> brands = brandService.getAll();
        return brandMapper.toDto(brands);
    }

    @GetMapping("/{id}")
    public BrandResponseDto getById(@PathVariable("id") Long id) {
        Brand brand = brandService.getById(id);
        return brandMapper.toDto(brand);
    }

    @PostMapping
    public BrandResponseDto post(@RequestBody @Validated(ValidationGroups.Create.class) BrandRequestDto request) {
        Brand entity = brandMapper.toEntity(request);
        Brand saved = brandService.save(entity);
        return brandMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public BrandResponseDto patch(@PathVariable("id") Long id,
                                  @RequestBody @Validated(ValidationGroups.Update.class) BrandRequestDto request) {
        Brand entity = brandMapper.toEntity(request);
        Brand updated = brandService.update(id, entity);
        return brandMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        brandService.deleteById(id);
    }
}
