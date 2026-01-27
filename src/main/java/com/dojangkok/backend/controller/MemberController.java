package com.dojangkok.backend.controller;

import com.dojangkok.backend.auth.jwt.CurrentMemberId;
import com.dojangkok.backend.common.dto.DataResponseDto;
import com.dojangkok.backend.common.dto.ResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.dto.member.UpdateNicknameRequestDto;
import com.dojangkok.backend.dto.member.UpdateNicknameResponseDto;
import com.dojangkok.backend.dto.member.ProfileResponseDto;
import com.dojangkok.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/nickname")
    public DataResponseDto<UpdateNicknameResponseDto> updateNickname(@CurrentMemberId Long memberId,
                                                                     @RequestBody UpdateNicknameRequestDto updateNicknameRequestDto) {
        UpdateNicknameResponseDto updateNicknameResponseDto = memberService.updateNickname(memberId, updateNicknameRequestDto);
        return new DataResponseDto<>(Code.SUCCESS, "닉네임 설정이 완료되었습니다.", updateNicknameResponseDto);
    }

    @GetMapping("/me")
    public DataResponseDto<ProfileResponseDto> getProfile(@CurrentMemberId Long memberId) {
        ProfileResponseDto profileResponseDto = memberService.getProfile(memberId);
        return new DataResponseDto<>(Code.SUCCESS, "프로필 조회에 성공하였습니다.", profileResponseDto);
    }

    @DeleteMapping("/me")
    public ResponseDto withdraw(@CurrentMemberId Long memberId) {
        memberService.withdraw(memberId);
        return new ResponseDto(Code.SUCCESS, "회원 탈퇴가 완료되었습니다.");
    }
}
