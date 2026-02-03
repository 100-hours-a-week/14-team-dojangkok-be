package com.dojangkok.backend.dto.member;

import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponseDto {

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("onboarding_status")
    private OnboardingStatus onboardingStatus;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}
