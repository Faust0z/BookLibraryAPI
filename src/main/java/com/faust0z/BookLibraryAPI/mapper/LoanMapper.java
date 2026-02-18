package com.faust0z.BookLibraryAPI.mapper;

import com.faust0z.BookLibraryAPI.dto.AdminLoanDTO;
import com.faust0z.BookLibraryAPI.dto.LoanDTO;
import com.faust0z.BookLibraryAPI.entity.LoanEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.name", target = "bookName")
    LoanDTO toDto(LoanEntity loan);

    List<LoanDTO> toDtoList(List<LoanEntity> loans);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.name", target = "bookName")
    AdminLoanDTO toAdminDto(LoanEntity loan);

    List<AdminLoanDTO> toAdminDtoList(List<LoanEntity> loans);
}