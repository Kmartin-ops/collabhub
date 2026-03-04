package com.collabhub.repository;

import com.collabhub.domain.RefreshToken;
import com.collabhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken (String token);

    // Revoke all active tokens for a user (on logout or password change)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllByUser(User user);
}

