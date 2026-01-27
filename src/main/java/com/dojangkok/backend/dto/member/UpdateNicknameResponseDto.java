package com.dojangkok.backend.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateNicknameResponseDto {

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("nickname")
    private String nickname;

}
