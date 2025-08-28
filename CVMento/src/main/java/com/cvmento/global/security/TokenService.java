package com.cvmento.global.security;

import com.cvmento.domain.auth.dto.TokenDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Getter
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String refreshTokenPrefix;
    private final String blacklistPrefix;
    private final String userSessionPrefix;

    public TokenService(JwtUtil jwtUtil,
                        RedisTemplate<String, Object> redisTemplate,
                        @Value("${redis.keys.refresh-token}") String refreshTokenPrefix,
                        @Value("${redis.keys.blacklist}") String blacklistPrefix,
                        @Value("${redis.keys.user-session}") String userSessionPrefix) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.refreshTokenPrefix = refreshTokenPrefix;
        this.blacklistPrefix = blacklistPrefix;
        this.userSessionPrefix = userSessionPrefix;
    }

    public TokenDto generateTokens(String userId, String email) {
        String accessToken = jwtUtil.generateAccessToken(userId, email);
        String refreshToken = jwtUtil.generateRefreshToken(userId, email);

        LocalDateTime accessTokenExpiresAt = jwtUtil.extractExpirationAsLocalDateTime(accessToken);
        LocalDateTime refreshTokenExpiresAt = jwtUtil.extractExpirationAsLocalDateTime(refreshToken);

        // Refresh Token을 Redis에 저장
        storeRefreshToken(userId, refreshToken, refreshTokenExpiresAt);

        // 사용자 세션 정보 저장
        storeUserSession(userId, refreshToken);

        return TokenDto.of(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt);
    }

    public TokenDto refreshAccessToken(String refreshToken) {
        if (!jwtUtil.isValidToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String userId = jwtUtil.extractUserId(refreshToken);
        String email = jwtUtil.extractEmail(refreshToken);

        // Redis에서 Refresh Token 검증
        if (!isValidRefreshToken(userId, refreshToken)) {
            throw new IllegalArgumentException("Refresh token not found or expired");
        }

        // 새로운 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(userId, email);
        LocalDateTime accessTokenExpiresAt = jwtUtil.extractExpirationAsLocalDateTime(newAccessToken);
        LocalDateTime refreshTokenExpiresAt = jwtUtil.extractExpirationAsLocalDateTime(refreshToken);

        return TokenDto.of(newAccessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt);
    }

    public void logout(String userId, String accessToken, String refreshToken) {
        // Access Token을 블랙리스트에 추가
        blacklistToken(accessToken);

        // Refresh Token을 블랙리스트에 추가
        if (refreshToken != null) {
            blacklistToken(refreshToken);
        }

        // Redis에서 Refresh Token 삭제
        removeRefreshToken(userId);

        // 사용자 세션 정보 삭제
        removeUserSession(userId);
    }

    public boolean isTokenBlacklisted(String token) {
        String jti = jwtUtil.extractJti(token);
        return redisTemplate.hasKey(blacklistPrefix + jti);
    }

    private void storeRefreshToken(String userId, String refreshToken, LocalDateTime expiresAt) {
        String key = refreshTokenPrefix + userId;
        long ttl = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.SECONDS);
    }

    private void storeUserSession(String userId, String refreshToken) {
        String key = userSessionPrefix + userId;
        LocalDateTime expiresAt = jwtUtil.extractExpirationAsLocalDateTime(refreshToken);
        long ttl = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();

        redisTemplate.opsForHash().put(key, "refreshToken", refreshToken);
        redisTemplate.opsForHash().put(key, "lastActivity", LocalDateTime.now().toString());
        redisTemplate.expire(key, Duration.ofSeconds(ttl));
    }

    private boolean isValidRefreshToken(String userId, String refreshToken) {
        String key = refreshTokenPrefix + userId;
        String storedToken = (String) redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }

    private void removeRefreshToken(String userId) {
        String key = refreshTokenPrefix + userId;
        redisTemplate.delete(key);
    }

    private void removeUserSession(String userId) {
        String key = userSessionPrefix + userId;
        redisTemplate.delete(key);
    }

    private void blacklistToken(String token) {
        String jti = jwtUtil.extractJti(token);
        Date expiration = jwtUtil.extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();

        if (ttl > 0) {
            String key = blacklistPrefix + jti;
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isUserSessionValid(String userId) {
        String key = userSessionPrefix + userId;
        return redisTemplate.hasKey(key);
    }

}