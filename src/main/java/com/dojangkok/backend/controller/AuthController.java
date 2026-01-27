package com.dojangkok.backend.controller;

import com.dojangkok.backend.common.dto.DataResponseDto;
import com.dojangkok.backend.common.dto.ResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.dto.auth.TokenExchangeRequestDto;
import com.dojangkok.backend.dto.auth.TokenExchangeResponseDto;
import com.dojangkok.backend.dto.auth.TokenExchangeResult;
import com.dojangkok.backend.dto.auth.TokenRefreshResponseDto;
import com.dojangkok.backend.dto.auth.TokenRefreshResult;
import com.dojangkok.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token")
    public DataResponseDto<TokenExchangeResponseDto> exchangeToken(@Valid @RequestBody TokenExchangeRequestDto request,
                                                                   HttpServletResponse response) {
        TokenExchangeResult result = authService.exchangeToken(request.getCode());
        response.addHeader(HttpHeaders.SET_COOKIE, result.getRefreshTokenCookie());
        return new DataResponseDto<>(Code.SUCCESS, "토큰 발급에 성공하였습니다.", result.getToken());
    }

    @PostMapping("/refresh")
    public DataResponseDto<TokenRefreshResponseDto> refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                                                                 HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new GeneralException(Code.INVALID_REFRESH_TOKEN);
        }

        TokenRefreshResult result = authService.refreshToken(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, result.getRefreshTokenCookie());

        return new DataResponseDto<>(Code.SUCCESS, "토큰 갱신에 성공하였습니다.", result.getToken());
    }

    @PostMapping("/logout")
    public ResponseDto logout(@CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
                              HttpServletResponse response) {
        String expiredCookie = authService.logout(cookieRefreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie);
        return new ResponseDto(Code.SUCCESS, "로그아웃이 완료되었습니다.");
    }


}
