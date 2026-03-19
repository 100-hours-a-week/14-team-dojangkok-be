package com.dojangkok.backend.dto.internal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InternalUserResponseDto {

    private final String userId;

    private final String nickname;

    private final String profileImageUrl;
}
