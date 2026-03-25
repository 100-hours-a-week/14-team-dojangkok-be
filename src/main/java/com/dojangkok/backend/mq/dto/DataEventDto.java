package com.dojangkok.backend.mq.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DataEventDto {

    private String type;
    @Builder.Default
    private Instant eventTimestamp = Instant.now();

    // USER_UPDATED, USER_DELETED
    private String userId;
    private String nickname;
    private String profileImageUrl;

    // PROPERTY_UPDATED, PROPERTY_DELETED
    private String propertyId;
    private String title;
    private String imageUrl;
    private String dealStatus;
}
