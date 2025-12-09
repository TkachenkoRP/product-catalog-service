package com.my.mapper;

import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Маппер для преобразования между сущностью User и соответствующими DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    /**
     * Обновляет поля целевой сущности User значениями из исходной сущности.
     * Игнорирует нулевые значения в исходной сущности.
     *
     * @param sourceUser исходная сущность User с новыми значениями
     * @param targetUser целевая сущность User для обновления
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateUser(User sourceUser, @MappingTarget User targetUser);

    /**
     * Преобразует DTO запроса в сущность User.
     *
     * @param request DTO с данными пользователя
     * @return сущность User
     */
    User toEntity(UserRequestDto request);

    /**
     * Преобразует сущность User в DTO ответа.
     *
     * @param entity сущность User
     * @return DTO с данными пользователя
     */
    UserResponseDto toDto(User entity);

    /**
     * Преобразует список сущностей User в список DTO ответов.
     *
     * @param entities список сущностей User
     * @return список DTO с данными пользователей
     */
    List<UserResponseDto> toDto(List<User> entities);
}
