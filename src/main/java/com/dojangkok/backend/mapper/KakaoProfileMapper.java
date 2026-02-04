package com.dojangkok.backend.mapper;

import com.dojangkok.backend.auth.oauth.dto.OAuthProfile;
import com.dojangkok.backend.domain.enums.Provider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KakaoProfileMapper implements OAuthProfileMapper {
    @Override
    public boolean supports(String registrationId) {
        return "kakao".equals(registrationId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuthProfile map(Map<String, Object> attrs) {
        String providerId = String.valueOf(attrs.get("id"));
        Map<String, Object> account = (Map<String, Object>) attrs.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());
        String username = (String) profile.get("nickname");
        String img = (String) profile.get("profile_image_url");

        return new OAuthProfile(Provider.KAKAO, providerId, username, img, attrs);
    }
}
