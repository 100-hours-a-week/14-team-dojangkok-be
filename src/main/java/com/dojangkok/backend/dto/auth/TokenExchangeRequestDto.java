package com.dojangkok.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenExchangeRequestDto {

    @NotBlank(message = "교환 코드는 필수입니다")
    @JsonProperty("code")
    private String code;
}
