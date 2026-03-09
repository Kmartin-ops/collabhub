package com.collabhub.service;

import com.collabhub.domain.RefreshToken;
import com.collabhub.domain.User;
import com.collabhub.dto.*;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.repository.UserRepository;
import com.collabhub.security.JwtService;
import com.collabhub.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @Mock
    AuthenticationManager authManager;
    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    AuthService authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@test.com", "DEVELOPER","password123!");
        user.setPasswordHash("hashed");

        refreshToken = new RefreshToken("refresh-token-xyz", user, java.time.Instant.now().plusSeconds(3600));
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("returns AuthResponse when email is new")
        void success() {
            when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtService.generateToken(anyString(), anyString())).thenReturn("access-token");
            when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

            AuthResponse resp = authService
                    .register(new RegisterRequest("Alice", "alice@test.com", "secret", "DEVELOPER"));

            assertThat(resp.accessToken()).isEqualTo("access-token");
            assertThat(resp.refreshToken()).isEqualTo("refresh-token-xyz");
            assertThat(resp.email()).isEqualTo("alice@test.com");
            assertThat(resp.role()).isEqualTo("DEVELOPER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("defaults role to DEVELOPER when null")
        void defaultsRole() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateToken(anyString(), anyString())).thenReturn("tok");
            when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

            AuthResponse resp = authService.register(new RegisterRequest("Alice", "alice@test.com", "secret", null));

            assertThat(resp.role()).isEqualTo("DEVELOPER");
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email already exists")
        void duplicateEmail() {
            when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

            assertThatThrownBy(
                    () -> authService.register(new RegisterRequest("Alice", "alice@test.com", "secret", "DEVELOPER")))
                            .isInstanceOf(DuplicateResourceException.class);

            verify(userRepository, never()).save(any());
        }
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("returns AuthResponse on valid credentials")
        void success() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(jwtService.generateToken(anyString(), anyString())).thenReturn("access-token");
            when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

            AuthResponse resp = authService.login(new LoginRequest("alice@test.com", "secret"));

            assertThat(resp.accessToken()).isEqualTo("access-token");
            assertThat(resp.email()).isEqualTo("alice@test.com");
            verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("propagates BadCredentialsException on wrong password")
        void badCredentials() {
            doThrow(new BadCredentialsException("bad")).when(authManager).authenticate(any());

            assertThatThrownBy(() -> authService.login(new LoginRequest("alice@test.com", "wrong")))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("rotates token and returns new AuthResponse")
        void success() {
            RefreshToken next = new RefreshToken("new-refresh-token", user, java.time.Instant.now().plusSeconds(3600));

            when(refreshTokenService.validateAndGet("refresh-token-xyz")).thenReturn(refreshToken);
            when(jwtService.generateToken(anyString(), anyString())).thenReturn("new-access-token");
            when(refreshTokenService.createRefreshToken(user)).thenReturn(next);

            AuthResponse resp = authService.refresh(new RefreshRequest("refresh-token-xyz"));

            assertThat(resp.accessToken()).isEqualTo("new-access-token");
            assertThat(resp.refreshToken()).isEqualTo("new-refresh-token");
        }
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("revokes all refresh tokens for user")
        void success() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

            authService.logout("alice@test.com");

            verify(refreshTokenService).revokeAllForUser(user);
        }

        @Test
        @DisplayName("throws when user not found")
        void userNotFound() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> authService.logout("nobody@test.com")).isInstanceOf(Exception.class);
        }
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("updates hash and revokes tokens on correct current password")
        void success() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("old-pass", "hashed")).thenReturn(true);
            when(passwordEncoder.encode("new-pass")).thenReturn("new-hashed");

            authService.changePassword("alice@test.com", new ChangePasswordRequest("old-pass", "new-pass"));

            verify(userRepository).save(user);
            verify(refreshTokenService).revokeAllForUser(user);
            assertThat(user.getPasswordHash()).isEqualTo("new-hashed");
        }

        @Test
        @DisplayName("throws IllegalArgumentException on wrong current password")
        void wrongCurrentPassword() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(
                    () -> authService.changePassword("alice@test.com", new ChangePasswordRequest("wrong", "new-pass")))
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("Current password is incorrect");

            verify(userRepository, never()).save(any());
        }
    }
}
