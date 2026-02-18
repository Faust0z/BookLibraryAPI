package com.faust0z.BookLibraryAPI.mapper;

import com.faust0z.BookLibraryAPI.dto.AdminBookDTO;
import com.faust0z.BookLibraryAPI.dto.BookDTO;
import com.faust0z.BookLibraryAPI.dto.CreateBookDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateBookDTO;
import com.faust0z.BookLibraryAPI.entity.BookEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    BookDTO toDto(BookEntity book);

    List<BookDTO> toDtoList(List<BookEntity> books);

    AdminBookDTO toAdminDto(BookEntity book);

    BookEntity toEntity(CreateBookDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBookFromDto(UpdateBookDTO dto, @MappingTarget BookEntity entity);
}