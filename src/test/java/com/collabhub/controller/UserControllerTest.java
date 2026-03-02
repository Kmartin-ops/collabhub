package com.collabhub.controller;

import com.collabhub.domain.User;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.mapper.UserMapperImpl;
import com.collabhub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserMapperImpl.class)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = new User("Alice", "alice@collabhub.com", "MANAGER");
        bob   = new User("Bob",   "bob@collabhub.com",   "DEVELOPER");
    }

    // ── GET /api/users ────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @DisplayName("should return 200 with list of users")
        void shouldReturn200WithUsers() throws Exception {
            when(userService.findAll()).thenReturn(List.of(alice, bob));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value("Alice"))
                    .andExpect(jsonPath("$[0].email").value("alice@collabhub.com"))
                    .andExpect(jsonPath("$[1].name").value("Bob"));
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyList() throws Exception {
            when(userService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should not expose password field")
        void shouldNotExposePassword() throws Exception {
            when(userService.findAll()).thenReturn(List.of(alice));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].password").doesNotExist());
        }
    }

    // ── GET /api/users/{email} ────────────────────────────────
    @Nested
    @DisplayName("GET /api/users/{email}")
    class GetUserByEmail {

        @Test
        @DisplayName("should return 200 with user when found")
        void shouldReturn200WhenFound() throws Exception {
            when(userService.getByEmail("alice@collabhub.com")).thenReturn(alice);

            mockMvc.perform(get("/api/users/alice@collabhub.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Alice"))
                    .andExpect(jsonPath("$.role").value("MANAGER"));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(userService.getByEmail(anyString()))
                    .thenThrow(new ResourceNotFoundException("User", "unknown@test.com"));

            mockMvc.perform(get("/api/users/unknown@test.com"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.path").value("/api/users/unknown@test.com"));
        }
    }

    // ── POST /api/users ───────────────────────────────────────
    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @DisplayName("should return 201 when user created successfully")
        void shouldReturn201OnSuccess() throws Exception {
            when(userService.createUser("Alice", "alice@collabhub.com", "MANAGER"))
                    .thenReturn(alice);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "Alice",
                                            "email", "alice@collabhub.com",
                                            "role", "MANAGER"))))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            containsString("/api/users/")))
                    .andExpect(jsonPath("$.name").value("Alice"));
        }

        @Test
        @DisplayName("should return 409 when email already exists")
        void shouldReturn409OnDuplicate() throws Exception {
            when(userService.createUser(anyString(), anyString(), anyString()))
                    .thenThrow(new DuplicateResourceException("User", "alice@collabhub.com"));

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "Alice",
                                            "email", "alice@collabhub.com",
                                            "role", "MANAGER"))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "",
                                            "email", "alice@collabhub.com",
                                            "role", "MANAGER"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.details[0]",
                            containsString("Name is required")));
        }

        @Test
        @DisplayName("should return 400 when email is invalid")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "Alice",
                                            "email", "not-an-email",
                                            "role", "MANAGER"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[0]",
                            containsString("valid email")));
        }

        @Test
        @DisplayName("should return 400 when role is invalid")
        void shouldReturn400WhenRoleInvalid() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "Alice",
                                            "email", "alice@collabhub.com",
                                            "role", "INTERN"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[0]",
                            containsString("ADMIN, MANAGER, or DEVELOPER")));
        }

        @Test
        @DisplayName("should return 400 with all errors when multiple fields invalid")
        void shouldReturnAllErrors() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "",
                                            "email", "bad",
                                            "role", "INTERN"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasSize(3)));
        }
    }
}