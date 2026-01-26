package com.dojangkok.backend.dto.lifestyle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LifestyleDto {

    @JsonProperty("lifestyle_item_id")
    private Long lifestyleItemId;

    @JsonProperty("lifestyle_item")
    private String lifestyleItem;
}
