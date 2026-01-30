package com.dojangkok.backend.auth.oauth.handler;

import com.dojangkok.backend.auth.token.RedisExchangeCodeStore;
import com.dojangkok.backend.auth.token.RedisExchangeCodeStore.ExchangeData;
import com.dojangkok.backend.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * OAuth2 로그인 성공 핸들러
 * - 일회성 교환 코드를 생성하여 프론트엔드로 리다이렉트
 * - 프론트엔드는 교환 코드로 토큰 교환 API 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RedisExchangeCodeStore exchangeCodeStore;
    private final MemberRepository memberRepository;

    @Value("${app.oauth2.success-redirect}")
    private String successRedirect;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        log.info("OAuth2 로그인 성공 - 핸들러 진입");

        DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
        Long memberId = ((Number) Objects.requireNonNull(principal).getAttributes().get("memberId")).longValue();
        boolean isNicknameExists = isNicknameExists(memberId);
        boolean isNewUser = (Boolean) principal.getAttributes().get("isNewUser");

        log.info("memberId: {}, isNewUser: {}", memberId, isNewUser);

        // 일회성 교환 코드 생성
        String exchangeCode = UUID.randomUUID().toString();
        exchangeCodeStore.save(exchangeCode, new ExchangeData(memberId, isNicknameExists, isNewUser));

        log.info("교환 코드 저장 완료: {}", exchangeCode);

        // 교환 코드만 쿼리 파라미터로 전달 (토큰 노출 방지)
        String redirectUrl = UriComponentsBuilder.fromUriString(successRedirect)
                .queryParam("code", exchangeCode)
                .build()
                .toUriString();

        log.info("리다이렉트 URL: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    private boolean isNicknameExists(Long memberId) {
        return memberRepository.existsNicknameById(memberId);
    }
}
