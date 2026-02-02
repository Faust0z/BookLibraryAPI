package com.faust0z.BookLibraryAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookDTO {
    @Schema(description = "Title of the book", example = "The Hobbit")
    @NotEmpty(message = "Book name cannot be empty.")
    private String name;

    @Schema(description = "Author of the book", example = "J. R. R. Tolkien")
    @NotEmpty(message = "Author name cannot be empty.")
    private String author;

    @Schema(description = "Publication date of the book in YYYY-MM-DD format", example = "1937-12-15")
    @NotNull(message = "Publication date is required.")
    @PastOrPresent(message = "Publication date cannot be in the future.")
    private LocalDate publicationDate;

    @Schema(description = "Number of copies of the book being inserted", example = "3")
    @NotNull(message = "Number of copies is required.")
    @Min(value = 0, message = "Copies cannot be negative.")
    private Integer copies;
}