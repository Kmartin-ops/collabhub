package com.collabhub.controller;

import com.collabhub.domain.User;
import com.collabhub.dto.CreateUserRequest;
import com.collabhub.dto.UserResponse;
import com.collabhub.mapper.UserMapper;
import com.collabhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public UserResponse getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByEmail(userDetails.getUsername());
        return userMapper.toResponse(user);
    }

    @GetMapping
    @Operation(summary = "Get all users")

    public List<UserResponse> getAllUsers() {
        return userService.findAll().stream().map(userMapper::toResponse).toList();
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get user by email")

    public UserResponse getUserByEmail(
            @Parameter(description = "User email", example = "alice@collabhub.com") @PathVariable String email) {
        return userMapper.toResponse(userService.getByEmail(email));
    }

    @PostMapping
    @Operation(summary = "Create a new user")

    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        var user = userService.createUser(request.name(), request.email(), request.role(), request.password());
        return ResponseEntity.created(URI.create("/api/users/" + user.getEmail())).body(userMapper.toResponse(user));
    }
}
