package com.dojangkok.backend.dto.auth;

import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenExchangeInfoDto {
    @JsonProperty("token")
    private TokenResponseDto token;

    @JsonProperty("onboarding_status")
    private OnboardingStatus onboardingStatus;
}