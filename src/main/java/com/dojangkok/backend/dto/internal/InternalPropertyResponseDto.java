package com.dojangkok.backend.dto.internal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InternalPropertyResponseDto {

    private final String propertyId;

    private final String title;

    private final String imageUrl;

    private final Long priceMain;

    private final Integer priceMonthly;

    private final String rentType;

    private final String dealStatus;
}
