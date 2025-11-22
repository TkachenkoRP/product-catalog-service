package com.my.mapper;

import com.my.dto.UserRequestDto;
import com.my.dto.UserResponseDto;
import com.my.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateUser(User sourceUser, @MappingTarget User targetUser);

    User copyUser(User sourceUser);

    User toEntity(UserRequestDto request);

    UserResponseDto toDto(User entity);

    List<UserResponseDto> toDto(List<User> entities);
}
