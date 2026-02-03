package com.faust0z.BookLibraryAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookDTO {
    @Schema(description = "The book's id", example = "e428d134-616f-41ae-b060-4284319a74ed")
    private UUID id;

    @Schema(description = "The book's name", example = "IT")
    private String name;

    @Schema(description = "The book's author", example = "Stephen King")
    private String author;

    @Schema(description = "The book's publication date in YYYY-MM-DD format", example = "1986-12-18")
    private LocalDate publicationDate;

    @Schema(description = "The amount of copies of the book", example = "4")
    private Integer copies;
}
