package com.dojangkok.backend.dto.lifestyle;

import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LifestyleResponseDto {

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("onboarding_status")
    private OnboardingStatus onboardingStatus;

    @JsonProperty("lifestyle_items")
    private List<LifestyleDto> lifestyleItems;
}
