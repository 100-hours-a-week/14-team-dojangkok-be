package com.dojangkok.backend.dto.lifestyle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LifestyleRequestDto {

    @JsonProperty("lifestyle_items")
    private List<String> lifestyleItems;
}
