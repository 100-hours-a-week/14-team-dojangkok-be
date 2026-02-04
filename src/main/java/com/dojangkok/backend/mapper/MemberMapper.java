package com.dojangkok.backend.mapper;

import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.dto.member.ProfileResponseDto;
import com.dojangkok.backend.dto.member.UpdateNicknameResponseDto;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public UpdateNicknameResponseDto toUpdateNicknameResponse(Member member, String nickname) {
        return UpdateNicknameResponseDto.builder()
                .memberId(member.getId())
                .onboardingStatus(member.getOnboardingStatus())
                .nickname(nickname)
                .build();
    }

    public ProfileResponseDto toProfileResponse(Member member) {
        return ProfileResponseDto.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .username(member.getUsername())
                .onboardingStatus(member.getOnboardingStatus())
                .profileImageUrl(member.getProfileImage())
                .build();
    }
}
