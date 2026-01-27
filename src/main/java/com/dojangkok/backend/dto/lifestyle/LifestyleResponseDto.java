package com.dojangkok.backend.dto.lifestyle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LifestyleResponseDto {

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("lifestyle_items")
    private List<LifestyleDto> lifestyleItems;
}
