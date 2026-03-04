package com.dojangkok.backend.dto.propertypost;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostVisibilityUpdateRequestDto {

    @NotNull(message = "is_hidden 값은 필수입니다.")
    @JsonProperty("is_hidden")
    private Boolean isHidden;
}
