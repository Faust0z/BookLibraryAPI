package com.faust0z.BookLibraryAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateLoanDTO {
    @Schema(description = "The user's id", example = "e428d134-616f-41ae-b060-4284319a74ed")
    @NotNull(message = "User ID is required.")
    private UUID userId;

    @Schema(description = "The book's id", example = "3a2ca8c9-a6da-4b98-824f-713c9be6dcf0")
    @NotNull(message = "Book ID is required.")
    private UUID bookId;
}