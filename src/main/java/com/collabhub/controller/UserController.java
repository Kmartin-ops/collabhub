package com.collabhub.controller;

import com.collabhub.dto.CreateUserRequest;
import com.collabhub.dto.UserResponse;
import com.collabhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) {
        // getByEmail() throws ResourceNotFoundException if missing — handler catches it
        return UserResponse.from(userService.getByEmail(email));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // @Valid triggers bean validation — handler catches MethodArgumentNotValidException
        // createUser() throws DuplicateResourceException — handler catches it
        var user = userService.createUser(
                request.name(), request.email(), request.role());
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getEmail()))
                .body(UserResponse.from(user));
    }
}