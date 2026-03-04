package com.dojangkok.backend.dto.propertypost;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PropertyPostFileAttachResponseDto {

    private List<PropertyPostFileAttachResponseItemDto> items;
}
