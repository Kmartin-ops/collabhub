package com.collabhub.security;

import com.collabhub.config.CollabHubProperties;
import com.collabhub.domain.RefreshToken;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final Logger LOG = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExiparationsMS;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, CollabHubProperties properties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExiparationsMS = properties.getSecurity().getRefreshExpirationsMS();

    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoke existing tokens for this user before issuing a new one
        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID().toString(), user,
                Instant.now().plusMillis(refreshExiparationsMS));
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        LOG.debug("Refresh token created for user={}", user.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateAndGet(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("RefreshToken", token));
        if (!refreshToken.isValid()) {
            LOG.warn("Invalid refresh token for user={}", refreshToken.getUser().getEmail());
            throw new IllegalStateException("Refresh token is expired or revoked");
        }
        return refreshToken;
    }

    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        LOG.info("All refresh tokens revoked for user={}", user.getEmail());
    }
}
