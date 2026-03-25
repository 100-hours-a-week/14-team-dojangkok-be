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

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class TestAuthController {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @GetMapping("/mock-login/{userId}")
    public String mockLogin(@PathVariable Long userId) {
        Member member = memberRepository.findById(userId).orElseGet(() -> {
            Member newMember = Member.createMember(
                    "tester" + userId,
                    "tester" + userId + "@test.com",
                    Role.USER,
                    "tester" + userId,
                    null
            );
            return memberRepository.save(newMember);
        });

        return jwtProvider.createAccessToken(member.getId());
    }
}
