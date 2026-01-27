package com.dojangkok.backend.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateNicknameRequestDto {

    @NotNull(message = "닉네임은 필수 입력값입니다.")
    @JsonProperty("nickname")
    private String nickname;
}
