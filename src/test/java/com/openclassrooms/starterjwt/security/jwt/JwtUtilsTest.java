package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    // secret jwt 1
    private static final String SECRET =
            "secretjwttest1bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";

    // secret jwt 2
    private static final String OTHER_SECRET =
            "secretjwttest2b8bdf42850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";

    private JwtUtils createJwtUtils(int expirationMs) {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", expirationMs);
        return jwtUtils;
    }

    private SecretKey keyFor(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateJwtToken_shouldGenerateValidTokenForUser() {
        // Arrange
        JwtUtils jwtUtils = createJwtUtils(3600000); // 1h
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("pwd")
                .admin(false)
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token))
                .isEqualTo("user@example.com");
    }

    @Test
    void validateJwtToken_shouldReturnFalse_whenSignatureIsInvalid() {
        // Arrange
        JwtUtils jwtUtils = createJwtUtils(3600000);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600000);

        String tokenWithOtherSecret = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(keyFor(OTHER_SECRET))
                .compact();

        // Act
        boolean valid = jwtUtils.validateJwtToken(tokenWithOtherSecret);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void validateJwtToken_shouldReturnFalse_whenTokenIsMalformed() {
        // Arrange
        JwtUtils jwtUtils = createJwtUtils(3600000);
        String malformed = "this-is-not-a-jwt";

        // Act
        boolean valid = jwtUtils.validateJwtToken(malformed);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void validateJwtToken_shouldReturnFalse_whenTokenIsExpired() {
        // Arrange
        JwtUtils jwtUtils = createJwtUtils(3600000);

        Date now = new Date();
        Date issuedAt = new Date(now.getTime() - 2_000);   // il y a 2 sec
        Date expiredAt = new Date(now.getTime() - 1_000);  // expiré il y a 1 sec

        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(keyFor(SECRET))
                .compact();

        // Act
        boolean valid = jwtUtils.validateJwtToken(expiredToken);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void getUserNameFromJwtToken_shouldReturnSubject() {
        // Arrange
        JwtUtils jwtUtils = createJwtUtils(3600000);
        String expectedUsername = "user@example.com";

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600000);

        String token = Jwts.builder()
                .subject(expectedUsername)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(keyFor(SECRET))
                .compact();

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertThat(username).isEqualTo(expectedUsername);
    }

    @Test
    void validateJwtToken_shouldReturnFalse_whenTokenIsNull() {
        // Arrange
        JwtUtils jwtUtils = createJwtUtils(3600000);

        // Act
        boolean valid = jwtUtils.validateJwtToken(null);

        // Assert
        assertThat(valid).isFalse();
    }
}