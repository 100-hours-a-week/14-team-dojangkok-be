package com.dojangkok.backend.dto.propertypost;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostVisibilityUpdateResponseDto {

    @JsonProperty("property_post_id")
    private Long propertyPostId;

    @JsonProperty("is_hidden")
    private boolean hidden;
}
