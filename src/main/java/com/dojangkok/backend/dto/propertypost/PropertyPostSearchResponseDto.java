package com.dojangkok.backend.dto.propertypost;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PropertyPostSearchResponseDto {

    @JsonProperty("total_count")
    private long totalCount;

    private int limit;

    @JsonProperty("hasNext")
    private boolean hasNext;

    @JsonProperty("next_cursor")
    private String nextCursor;

    @JsonProperty("items")
    private List<PropertyPostListItemDto> items;
}
