package com.faust0z.BookLibraryAPI.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LoanDTO {
    private UUID id;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private UUID userId;
    private UUID bookId;
    private String bookName;
}