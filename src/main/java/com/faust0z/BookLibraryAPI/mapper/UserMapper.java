package com.faust0z.BookLibraryAPI.mapper;

import com.faust0z.BookLibraryAPI.dto.AdminUserDTO;
import com.faust0z.BookLibraryAPI.dto.MyUserDetailsDTO;
import com.faust0z.BookLibraryAPI.dto.RegisterRequestDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateUserDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDTO toDto(UserEntity user);

    AdminUserDTO toAdminDto(UserEntity user);

    List<AdminUserDTO> toAdminDtoList(List<UserEntity> users);

    MyUserDetailsDTO toMyDetailsDto(UserEntity user);

    @Mapping(target = "password", ignore = true)
    UserEntity toEntity(RegisterRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserDTO dto, @MappingTarget UserEntity entity);
}