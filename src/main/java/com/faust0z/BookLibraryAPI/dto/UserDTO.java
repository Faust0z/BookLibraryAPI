package com.faust0z.BookLibraryAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    @Schema(description = "The user's id", example = "1c70cf24-cea0-41a9-a4e3-38628c075a43")
    private UUID id;

    @Schema(description = "The user's name", example = "John")
    private String name;

    @Schema(description = "The user's email", example = "John.doe@example.com")
    private String email;
}
