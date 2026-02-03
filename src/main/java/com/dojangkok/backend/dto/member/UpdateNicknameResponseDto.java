package com.dojangkok.backend.dto.member;

import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateNicknameResponseDto {

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("onboarding_status")
    private OnboardingStatus onboardingStatus;

    @JsonProperty("nickname")
    private String nickname;

}
