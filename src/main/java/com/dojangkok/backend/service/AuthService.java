package com.dojangkok.backend.service;

import com.dojangkok.backend.auth.jwt.JwtProvider;
import com.dojangkok.backend.auth.token.RedisExchangeCodeStore;
import com.dojangkok.backend.auth.token.RedisExchangeCodeStore.ExchangeData;
import com.dojangkok.backend.auth.token.RedisRefreshTokenStore;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.dto.auth.TokenExchangeResponseDto;
import com.dojangkok.backend.dto.auth.TokenExchangeResult;
import com.dojangkok.backend.dto.auth.TokenRefreshResponseDto;
import com.dojangkok.backend.dto.auth.TokenRefreshResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final RedisExchangeCodeStore exchangeCodeStore;
    private final RedisRefreshTokenStore refreshTokenStore;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenExchangeResult exchangeToken(String code) {
        ExchangeData data = exchangeCodeStore.consume(code)
                .orElseThrow(() -> new GeneralException(Code.INVALID_EXCHANGE_CODE));

        Long memberId = data.memberId();
        boolean isNewUser = data.isNewUser();

        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);

        refreshTokenStore.save(memberId, refreshToken, Duration.ofDays(jwtProvider.getRefreshExpDays()));

        TokenExchangeResponseDto token = TokenExchangeResponseDto.builder()
                .accessToken(accessToken)
                .expiresIn(jwtProvider.getAccessExpMin() * 60)
                .isNewUser(isNewUser)
                .build();

        return TokenExchangeResult.builder()
                .token(token)
                .refreshTokenCookie(createRefreshTokenCookie(refreshToken))
                .build();
    }

    @Transactional
    public TokenRefreshResult refreshToken(String refreshToken) {

        Long memberId = jwtProvider.getMemberIdFromRefreshToken(refreshToken); // 서명 검증 포함
        if (memberId == null) {
            throw new GeneralException(Code.INVALID_REFRESH_TOKEN);
        }

        if (!refreshTokenStore.validate(memberId, refreshToken)) {
            refreshTokenStore.delete(memberId); // member 기준으로 통일
            throw new GeneralException(Code.TOKEN_REUSE_DETECTED);
        }

        String newAccessToken = jwtProvider.createAccessToken(memberId);
        String newRefreshToken = jwtProvider.createRefreshToken(memberId);

        refreshTokenStore.save(memberId, newRefreshToken, Duration.ofDays(jwtProvider.getRefreshExpDays()));

        TokenRefreshResponseDto token = TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtProvider.getAccessExpMin() * 60)
                .build();

        return TokenRefreshResult.builder()
                .token(token)
                .refreshTokenCookie(createRefreshTokenCookie(newRefreshToken))
                .build();
    }

    @Transactional
    public String logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenStore.deleteByToken(refreshToken);
        }
        return createExpiredRefreshTokenCookie();
    }

    private String createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(jwtProvider.getRefreshExpDays()))
                .build()
                .toString();
    }

    private String createExpiredRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build()
                .toString();
    }
}
