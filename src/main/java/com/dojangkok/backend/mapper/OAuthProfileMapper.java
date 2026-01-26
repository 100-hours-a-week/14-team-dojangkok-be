package com.dojangkok.backend.mapper;

import com.dojangkok.backend.auth.oauth.dto.OAuthProfile;

import java.util.Map;

public interface OAuthProfileMapper {
    boolean supports(String registrationId);
    OAuthProfile map(Map<String, Object> attributes);
}

