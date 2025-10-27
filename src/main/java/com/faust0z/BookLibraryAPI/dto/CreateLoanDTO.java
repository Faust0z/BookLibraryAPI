package com.faust0z.BookLibraryAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateLoanDTO {

    @NotNull(message = "User ID is required.")
    private UUID userId;

    @NotNull(message = "Book ID is required.")
    private UUID bookId;
}