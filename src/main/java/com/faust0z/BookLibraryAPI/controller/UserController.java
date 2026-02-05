package com.faust0z.BookLibraryAPI.controller;

import com.faust0z.BookLibraryAPI.dto.AdminUserDTO;
import com.faust0z.BookLibraryAPI.dto.MyUserDetailsDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateUserDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Library's users management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        List<AdminUserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get a single user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable UUID userId) {
        AdminUserDTO user = userService.getUserbyId(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Get the current logged user",
            description = "This endpoint gets the data by using the JWT, so there are no parameters required"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully"),
    })
    @GetMapping("/me")
    public ResponseEntity<MyUserDetailsDTO> getMyProfile(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        MyUserDetailsDTO user = userService.getMyDetails(userId);

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Patch the current user's data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PatchMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
                                                   @Valid @RequestBody UpdateUserDTO userData) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        UserDTO updatedUser = userService.updateUser(userId, userData);

        return ResponseEntity.ok(updatedUser);
    }
}

