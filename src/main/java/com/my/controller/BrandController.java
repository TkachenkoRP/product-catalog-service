package com.my.controller;

import com.my.annotation.Audition;
import com.my.dto.BrandRequestDto;
import com.my.dto.BrandResponseDto;
import com.my.mapper.BrandMapper;
import com.my.model.Brand;
import com.my.service.BrandService;
import com.my.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Бренды", description = "API для управления брендами")
public class BrandController {
    private final BrandService brandService;
    private final BrandMapper brandMapper;

    @Operation(summary = "Получить все бренды", description = "Возвращает список всех брендов")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка брендов")
    @GetMapping
    public List<BrandResponseDto> getAll() {
        List<Brand> brands = brandService.getAll();
        return brandMapper.toDto(brands);
    }

    @Operation(summary = "Получить бренд по ID", description = "Возвращает бренд по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд найден"),
            @ApiResponse(responseCode = "404", description = "Бренд не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public BrandResponseDto getById(@PathVariable("id") Long id) {
        Brand brand = brandService.getById(id);
        return brandMapper.toDto(brand);
    }

    @Operation(summary = "Создать новый бренд", description = "Создает новый бренд")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content)
    })
    @PostMapping
    public BrandResponseDto post(@RequestBody @Validated(ValidationGroups.Create.class) BrandRequestDto request) {
        Brand entity = brandMapper.toEntity(request);
        Brand saved = brandService.save(entity);
        return brandMapper.toDto(saved);
    }

    @Operation(summary = "Обновить бренд", description = "Обновляет существующий бренд")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Бренд не найдена", content = @Content)
    })
    @PatchMapping("/{id}")
    public BrandResponseDto patch(@PathVariable("id") Long id,
                                  @RequestBody @Validated(ValidationGroups.Update.class) BrandRequestDto request) {
        Brand entity = brandMapper.toEntity(request);
        Brand updated = brandService.update(id, entity);
        return brandMapper.toDto(updated);
    }

    @Operation(summary = "Удалить бренд", description = "Удаляет бренд по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд успешно удален"),
            @ApiResponse(responseCode = "404", description = "Бренд не найден", content = @Content),
            @ApiResponse(responseCode = "409", description = "Бренд имеет ссылки", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        brandService.deleteById(id);
    }
}
