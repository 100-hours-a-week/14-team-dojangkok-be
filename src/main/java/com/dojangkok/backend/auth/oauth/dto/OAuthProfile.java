package com.dojangkok.backend.auth.oauth.dto;

import com.dojangkok.backend.domain.enums.Provider;

import java.util.Map;

public record OAuthProfile(
        Provider provider,
        String providerId,
        String profileImageUrl,
        Map<String, Object> attributes
) {}

