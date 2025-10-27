package com.faust0z.BookLibraryAPI.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookDTO {
    private UUID id;
    private String name;
    private String author;
    private LocalDate publicationDate;
    private int copies;
}