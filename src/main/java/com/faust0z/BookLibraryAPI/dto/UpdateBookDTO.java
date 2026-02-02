package com.faust0z.BookLibraryAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateBookDTO {
    @Schema(description = "Title of the book", example = "The Hobbit")
    private String name;

    @Schema(description = "Author of the book", example = "J. R. R. Tolkien")
    private String author;

    @Schema(description = "Publication date of the book in YYYY-MM-DD format", example = "1937-12-15")
    @PastOrPresent(message = "Publication date cannot be in the future.")
    private LocalDate publicationDate;

    @Schema(description = "Number of copies of the book being inserted", example = "3")
    @Min(value = 0, message = "No negative amount of copies allowed.")
    private Integer copies;
}
