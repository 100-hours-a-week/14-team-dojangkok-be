package com.dojangkok.backend.auth.jwt;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@Getter
public class JwtProvider {

    private static final String CLAIM_TYP = "typ";
    private static final String TYP_ACCESS = "access";
    private static final String TYP_REFRESH = "refresh";

    private final String issuer;
    private final SecretKey key;
    private final int accessExpMin;
    private final int refreshExpDay;

    public JwtProvider(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.access-exp-min}") int accessExpMin,
            @Value("${jwt.refresh-exp-day}") int refreshExpDay
    ) {
        this.issuer = issuer;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.accessExpMin = accessExpMin;
        this.refreshExpDay = refreshExpDay;
    }

    public String createAccessToken(Long memberId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(accessExpMin))))
                .claim(CLAIM_TYP, TYP_ACCESS)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofDays(refreshExpDay))))
                .claim(CLAIM_TYP, TYP_REFRESH)
                .signWith(key)
                .compact();
    }

    public int getRefreshExpDays() {
        return refreshExpDay;
    }

    /**
     * Refresh Token에서 memberId 추출 (예외 던짐)
     */
    public Long getMemberIdFromRefreshToken(String refreshToken) {
        return tryExtractMemberIdFromRefreshToken(refreshToken)
                .orElseThrow(() -> new GeneralException(Code.INVALID_REFRESH_TOKEN));
    }

    /**
     * Access Token에서 memberId 추출
     * - 파싱/만료/서명/typ 검증 실패 시 empty
     * - 필터에서 일단 통과시키되 인증만 안 태우는 용도
     */
    public Optional<Long> tryExtractMemberIdFromAccessToken(String accessToken) {
        try {
            Claims claims = parseAndValidateType(accessToken, TYP_ACCESS);
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (ExpiredJwtException e) {
            log.debug("만료된 access 토큰: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 access 토큰: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Refresh Token에서 memberId 추출
     */
    public Optional<Long> tryExtractMemberIdFromRefreshToken(String refreshToken) {
        try {
            Claims claims = parseAndValidateType(refreshToken, TYP_REFRESH);
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (ExpiredJwtException e) {
            log.debug("만료된 refresh 토큰: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 refresh 토큰: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 토큰 파싱 + typ 검증까지 한 번에 처리
     */
    private Claims parseAndValidateType(String token, String expectedType) {
        Claims claims = parseSignedClaims(token);

        String type = claims.get(CLAIM_TYP, String.class);
        if (!expectedType.equals(type)) {
            // access/refresh 타입이 섞인 경우
            throw new JwtException("Invalid token type: expected=" + expectedType + ", actual=" + type);
        }

        return claims;
    }

    /**
     * 공통 Claims 파싱 (서명 검증 포함)
     */
    private Claims parseSignedClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
