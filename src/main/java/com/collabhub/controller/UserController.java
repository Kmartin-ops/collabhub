package com.collabhub.controller;

import com.collabhub.dto.CreateUserRequest;
import com.collabhub.dto.UserResponse;
import com.collabhub.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")  // base path for all methods in this controller
public class UserController {

    private final UserService userService;

    // Spring injects UserService automatically
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    // GET /api/users/{email}
    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(UserResponse::from)          // if found, wrap in 200
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // if not, 404
    }

    // POST /api/users  → 201 Created
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        try {
            var user = userService.createUser(
                    request.name(), request.email(), request.role());
            UserResponse response = UserResponse.from(user);
            return ResponseEntity
                    .created(URI.create("/api/users/" + user.getEmail()))
                    .body(response);
        } catch (IllegalArgumentException e) {
            // duplicate email
            return ResponseEntity.status(409).build();
        }
    }
}