package com.dojangkok.backend.mq.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DataEventDto {

    private String type;

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
