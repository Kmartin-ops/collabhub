package com.collabhub.service;

import com.collabhub.domain.User;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("Alice", "alice@test.com", "DEVELOPER", "password123!");
        user.setId(userId);
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("saves and returns new user")
        void success() {
            when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(user);

            User result = userService.createUser("Alice", "alice@test.com", "DEVELOPER", "password123!");

            assertThat(result.getEmail()).isEqualTo("alice@test.com");
            assertThat(result.getRole()).isEqualTo("DEVELOPER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email already exists")
        void duplicateEmail() {
            when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

            assertThatThrownBy(() ->
                    userService.createUser("Alice", "alice@test.com", "DEVELOPER", "password123!"))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(userRepository, never()).save(any());
        }
    }

    // ── getByEmail ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getByEmail()")
    class GetByEmail {

        @Test
        @DisplayName("returns user when found")
        void found() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

            User result = userService.getByEmail("alice@test.com");

            assertThat(result.getEmail()).isEqualTo("alice@test.com");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when missing")
        void notFound() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getByEmail("nobody@test.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns user when found")
        void found() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            User result = userService.getById(userId);

            assertThat(result.getId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when missing")
        void notFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getById(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("returns all users")
        void success() {
            when(userRepository.findAll()).thenReturn(List.of(user));

            List<User> results = userService.findAll();

            assertThat(results).hasSize(1).contains(user);
        }

        @Test
        @DisplayName("returns empty list when no users")
        void empty() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertThat(userService.findAll()).isEmpty();
        }
    }

    // ── exists ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("exists()")
    class Exists {

        @Test
        @DisplayName("returns true when email is registered")
        void exists() {
            when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);
            assertThat(userService.exists("alice@test.com")).isTrue();
        }

        @Test
        @DisplayName("returns false when email is not registered")
        void doesNotExist() {
            when(userRepository.existsByEmail("nobody@test.com")).thenReturn(false);
            assertThat(userService.exists("nobody@test.com")).isFalse();
        }
    }

    // ── setPassword ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("setPassword()")
    class SetPassword {

        @Test
        @DisplayName("encodes password and saves user")
        void success() {
            when(passwordEncoder.encode("raw-password")).thenReturn("encoded-hash");

            userService.setPassword(user, "raw-password");

            assertThat(user.getPasswordHash()).isEqualTo("encoded-hash");
            verify(userRepository).save(user);
        }
    }
}