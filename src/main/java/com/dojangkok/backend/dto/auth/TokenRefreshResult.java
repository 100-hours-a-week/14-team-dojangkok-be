package com.dojangkok.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenRefreshResult {

    @JsonProperty("token")
    private TokenRefreshResponseDto token;

    private String refreshTokenCookie;
}
