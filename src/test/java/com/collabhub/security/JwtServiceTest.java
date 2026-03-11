/*
package com.collabhub.security;

import com.collabhub.config.CollabHubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String TEST_SECRET_BASE64 = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        CollabHubProperties properties = new CollabHubProperties();
        CollabHubProperties.Security security = new CollabHubProperties.Security();
        security.setJwtSecret(TEST_SECRET_BASE64);
        security.setJwtExpirationMs(60_000);
        properties.setSecurity(security);

        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("should generate token and extract email/role")
    void shouldGenerateAndExtractClaims() {
        String token = jwtService.generateToken("alice@collabhub.com", "MANAGER");

        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@collabhub.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("MANAGER");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("should return false for malformed token")
    void shouldRejectMalformedToken() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("should return false for tampered token")
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken("bob@collabhub.com", "DEVELOPER");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}
*/
package com.collabhub.security;

import com.collabhub.config.CollabHubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    // Base64-encoded 32-byte key
    private static final String TEST_SECRET_BASE64 = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        CollabHubProperties properties = new CollabHubProperties();
        CollabHubProperties.Security security = new CollabHubProperties.Security();
        security.setJwtSecret(TEST_SECRET_BASE64);
        security.setJwtExpirationMs(60_000); // 1 minute
        properties.setSecurity(security);

        jwtService = new JwtService(properties);
        jwtService.init(); // <-- Important: initialize signingKey and expiration
    }

    @Test
    @DisplayName("should generate token and extract email/role")
    void shouldGenerateAndExtractClaims() {
        String token = jwtService.generateToken("alice@collabhub.com", "MANAGER");

        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@collabhub.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("MANAGER");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("should return false for malformed token")
    void shouldRejectMalformedToken() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("should return false for tampered token")
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken("bob@collabhub.com", "DEVELOPER");

        // Tamper the payload segment to ensure the signature no longer matches.
        String[] parts = token.split("\\.");
        char[] payloadChars = parts[1].toCharArray();
        int idx = payloadChars.length / 2;
        payloadChars[idx] = payloadChars[idx] == 'a' ? 'b' : 'a';
        parts[1] = new String(payloadChars);
        String tampered = String.join(".", parts);

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}
