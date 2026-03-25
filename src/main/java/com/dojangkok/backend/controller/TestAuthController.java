package com.dojangkok.backend.controller;

import com.dojangkok.backend.auth.jwt.JwtProvider;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.domain.enums.Role;
import com.dojangkok.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class TestAuthController {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @GetMapping("/mock-login/{userId}")
    public String mockLogin(@PathVariable Long userId) {
        // 1) ID로 조회
        Optional<Member> byId = memberRepository.findById(userId);
        if (byId.isPresent()) {
            return jwtProvider.createAccessToken(byId.get().getId());
        }

        // 2) 없으면 nickname 중복 체크 후 생성
        String nickname = "tester" + userId;
        String email = "tester" + userId + "@test.com";

        if (memberRepository.existsByNickname(nickname)) {
            // 이미 다른 ID로 같은 nickname이 존재 → 타임스탬프 추가
            nickname = "tester" + userId + "_" + System.currentTimeMillis();
            email = "tester" + userId + "_" + System.currentTimeMillis() + "@test.com";
        }

        Member newMember = Member.createMember(nickname, email, Role.USER, nickname, null);
        memberRepository.save(newMember);

        return jwtProvider.createAccessToken(newMember.getId());
    }
}
