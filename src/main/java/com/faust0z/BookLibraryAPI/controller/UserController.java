package com.faust0z.BookLibraryAPI.controller;

import com.faust0z.BookLibraryAPI.dto.UpdateUserDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get a single user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        UserDTO user = userService.getUserbyId(userId);
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
    public ResponseEntity<UserDTO> getMyProfile(@AuthenticationPrincipal @Parameter(hidden = true) UserEntity currentUser) {
        return ResponseEntity.ok(userService.convertToDto(currentUser));
    }

    @Operation(summary = "Patch the current user's data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PatchMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@AuthenticationPrincipal @Parameter(hidden = true) UserEntity currentUser,
                                                   @Valid @RequestBody UpdateUserDTO userData) {
        UserDTO updatedUser = userService.updateUser(currentUser.getId(), userData);
        return ResponseEntity.ok(updatedUser);
    }
}

