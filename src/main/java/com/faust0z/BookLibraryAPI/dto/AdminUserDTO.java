package com.faust0z.BookLibraryAPI.dto;

import com.faust0z.BookLibraryAPI.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminUserDTO {
    @Schema(description = "The user's id", example = "1c70cf24-cea0-41a9-a4e3-38628c075a43")
    private UUID id;

    @Schema(description = "The user's name", example = "John")
    private String name;

    @Schema(description = "The user's email", example = "John.doe@example.com")
    private String email;

    @Schema(description = "The time and date the user was created", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "The time and date the user was last modified", example = "2024-01-01T10:00:00")
    private LocalDateTime lastModifiedAt;

    @Schema(description = "The user's role", example = "ADMIN")
    private Role role;
}
