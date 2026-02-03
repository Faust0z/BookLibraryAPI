package com.faust0z.BookLibraryAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LoanDTO {
    @Schema(description = "The user's id", example = "e428d134-616f-41ae-b060-4284319a74ed")
    private UUID id;

    @Schema(description = "The issued date of the loan in YYYY-MM-DD format", example = "2026-02-03")
    private LocalDate loanDate;

    @Schema(description = "The due date of the loan in YYYY-MM-DD format", example = "2026-02-25")
    private LocalDate dueDate;

    @Schema(description = "The date the loan was returned in YYYY-MM-DD format", example = "2026-02-20")
    private LocalDate returnDate;

    @Schema(description = "The ID of the user who requested the loan", example = "9fe3ca19-94ee-4814-ad93-f373cee25aa1")
    private UUID userId;

    @Schema(description = "The ID of the book loaned", example = "9fe3ca19-94ee-4814-ad93-f373cee25aa1")
    private UUID bookId;

    @Schema(description = "The name of the book loaned", example = "The Martian")
    private String bookName;
}