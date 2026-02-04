package com.dojangkok.backend.dto.auth;

import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenExchangeResponseDto {

    @JsonProperty("exchange_info")
    private TokenExchangeInfoDto tokenExchangeInfoDto;

    private String refreshToken;

}
