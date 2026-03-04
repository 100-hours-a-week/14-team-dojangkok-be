package com.dojangkok.backend.dto.propertypost;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PropertyPostSearchCountResponseDto {

    private long count;
}
