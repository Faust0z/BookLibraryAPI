package com.faust0z.BookLibraryAPI.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateBookDTO {
    private String name;

    private String author;

    private LocalDate publicationDate;

    private Integer copies;
}
