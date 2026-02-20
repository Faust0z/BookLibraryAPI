package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.entity.Role;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "9a4f4e35e719147d92631551812487da725fbda8e0c03459393049ad5524655f";

    @BeforeEach
    void setUp() {
        long expiration = 3600000;
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    void generateToken_ShouldCreateValidSignedJwt() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("test@example.com");
        userEntity.setRole(Role.USER);

        String token = jwtTokenProvider.generateToken(userEntity);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(userId.toString());
        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    void extractAuthorities_ShouldReturnCorrectRoles() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setRole(Role.ADMIN);

        String token = jwtTokenProvider.generateToken(userEntity);
        List<String> authorities = jwtTokenProvider.extractAuthorities(token);

        assertThat(authorities)
                .hasSize(1)
                .contains("ROLE_ADMIN");
    }

    @Test
    void extractUserId_ShouldReturnSubjectClaim() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setRole(Role.USER);

        String token = jwtTokenProvider.generateToken(userEntity);
        String extractedId = jwtTokenProvider.extractUserId(token);

        assertThat(extractedId).isEqualTo(userId.toString());
    }

    @Test
    void isTokenValid_WhenExpired_ShouldThrowException() throws InterruptedException {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, 1);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setRole(Role.USER);

        String token = shortLivedProvider.generateToken(userEntity);

        Thread.sleep(10);

        assertThatThrownBy(() -> shortLivedProvider.isTokenValid(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}