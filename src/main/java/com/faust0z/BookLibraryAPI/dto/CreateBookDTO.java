package com.faust0z.BookLibraryAPI.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookDTO {
    @NotEmpty(message = "Book name cannot be empty.")
    private String name;

    @NotEmpty(message = "Author name cannot be empty.")
    private String author;

    @NotNull(message = "Publication date is required.")
    @PastOrPresent(message = "Publication date cannot be in the future.")
    private LocalDate publicationDate;

    @NotNull(message = "Number of copies is required.")
    @Min(value = 0, message = "Copies cannot be negative.")
    private Integer copies;
}