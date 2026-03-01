package com.collabhub.controller;

import com.collabhub.dto.CreateUserRequest;
import com.collabhub.dto.ErrorResponse;
import com.collabhub.dto.UserResponse;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Returns all registered users in CollabHub"
    )
    @ApiResponse(responseCode = "200", description = "List of users returned")
    public List<UserResponse> getAllUsers() {
        return userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{email}")
    @Operation(
            summary = "Get user by email",
            description = "Looks up a single user by their email address"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public UserResponse getUserByEmail(
            @Parameter(description = "User's email address", example = "alice@collabhub.com")
            @PathVariable String email) {
        return UserResponse.from(userService.getByEmail(email));
    }

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Registers a new user. Email must be unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with this email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        var user = userService.createUser(
                request.name(), request.email(), request.role());
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getEmail()))
                .body(UserResponse.from(user));
    }
}