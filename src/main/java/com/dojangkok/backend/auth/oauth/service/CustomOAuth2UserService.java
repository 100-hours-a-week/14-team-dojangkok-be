package com.dojangkok.backend.auth.oauth.service;

import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.domain.SocialAuth;
import com.dojangkok.backend.domain.enums.Role;
import com.dojangkok.backend.auth.oauth.dto.OAuthProfile;
import com.dojangkok.backend.mapper.OAuthProfileMapper;
import com.dojangkok.backend.repository.MemberRepository;
import com.dojangkok.backend.repository.SocialAuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dojangkok.backend.domain.SocialAuth.createSocialAuth;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final List<OAuthProfileMapper> mappers;
    private final SocialAuthRepository socialAuthRepository;
    private final MemberRepository memberRepository;

    /**
     * 카카오 서버로부터 사용자 정보 받음
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(req);

        String registrationId = req.getClientRegistration().getRegistrationId();
        OAuthProfileMapper mapper = mappers.stream()
                .filter(m -> m.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> new OAuth2AuthenticationException("Unsupported provider: " + registrationId));

        OAuthProfile profile = mapper.map(oAuth2User.getAttributes());

        // 신규/기존 회원 구분
        Optional<SocialAuth> existingSocialAuth = socialAuthRepository
                .findByProviderAndProviderId(profile.provider(), profile.providerId());
        
        boolean isNewUser = existingSocialAuth.isEmpty();
        
        SocialAuth socialAuth;
        if (isNewUser) {
            socialAuth = createMemberAndLink(profile);
        } else {
            socialAuth = existingSocialAuth.get();
            socialAuth.updateAttributes(profile.attributes());
            socialAuth.getMember().updateLastLoggedInAt();
        }

        Long memberId = socialAuth.getMember().getId();

        Map<String, Object> principalAttrs = new HashMap<>(oAuth2User.getAttributes());
        principalAttrs.put("memberId", memberId);
        principalAttrs.put("provider", profile.provider().name());
        principalAttrs.put("isNewUser", isNewUser);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                principalAttrs,
                "memberId"
        );
    }

    private SocialAuth createMemberAndLink(OAuthProfile profile) {
        Member member = Member.createMember(null, null, Role.USER, profile.username(), profile.profileImageUrl());
        memberRepository.save(member);

        SocialAuth socialAuth = createSocialAuth(
                member, 
                profile.provider(), 
                profile.providerId(), 
                null,
                profile.attributes()
        );
        return socialAuthRepository.save(socialAuth);
    }
}

