package com.collabhub.security;

import com.collabhub.config.CollabHubProperties;
import com.collabhub.domain.RefreshToken;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        CollabHubProperties properties = new CollabHubProperties();
        CollabHubProperties.Security security = new CollabHubProperties.Security();
        security.setRefreshExpirationsMS(3_600_000);
        properties.setSecurity(security);

        refreshTokenService = new RefreshTokenService(refreshTokenRepository, properties);

        user = new User("Alice", "alice@collabhub.com", "MANAGER","password123!");
        user.setPasswordHash("hashed");
    }

    @Test
    @DisplayName("createRefreshToken should revoke old tokens and persist a new one")
    void shouldCreateRefreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(user);

        verify(refreshTokenRepository).revokeAllByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getExpiresAt()).isAfter(Instant.now().minusSeconds(1));
    }

    @Test
    @DisplayName("validateAndGet should return token when valid")
    void shouldValidateValidToken() {
        RefreshToken token = new RefreshToken("valid-token", user, Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        RefreshToken resolved = refreshTokenService.validateAndGet("valid-token");

        assertThat(resolved).isEqualTo(token);
    }

    @Test
    @DisplayName("validateAndGet should throw when token not found")
    void shouldThrowWhenMissing() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateAndGet("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("validateAndGet should throw when token is expired or revoked")
    void shouldThrowWhenInvalid() {
        RefreshToken expired = new RefreshToken("expired-token", user, Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.validateAndGet("expired-token"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("expired or revoked");
    }

    @Test
    @DisplayName("revokeAllForUser should delegate to repository")
    void shouldRevokeAllForUser() {
        refreshTokenService.revokeAllForUser(user);

        verify(refreshTokenRepository).revokeAllByUser(user);
    }
}
