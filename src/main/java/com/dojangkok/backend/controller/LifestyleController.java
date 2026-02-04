package com.dojangkok.backend.controller;

import com.dojangkok.backend.auth.jwt.CurrentMemberId;
import com.dojangkok.backend.common.dto.DataResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.dto.lifestyle.LifestyleRequestDto;
import com.dojangkok.backend.dto.lifestyle.LifestyleResponseDto;
import com.dojangkok.backend.service.LifestyleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lifestyles")
public class LifestyleController {

    private final LifestyleService lifestyleService;

    @PostMapping
    public DataResponseDto<LifestyleResponseDto> createLifestyle(@CurrentMemberId Long memberId,
                                                                 @Valid @RequestBody LifestyleRequestDto lifestyleRequestDto) {
        LifestyleResponseDto lifestyleResponseDto = lifestyleService.createLifestyle(memberId, lifestyleRequestDto);
        return new DataResponseDto<>(Code.CREATED_SUCCESS, "라이프스타일 생성에 성공하였습니다.", lifestyleResponseDto);
    }

    @GetMapping
    public DataResponseDto<LifestyleResponseDto> getLifestyle(@CurrentMemberId Long memberId) {
        LifestyleResponseDto lifestyleResponseDto = lifestyleService.getLifestyle(memberId);
        return new DataResponseDto<>(Code.SUCCESS, "라이프스타일 조회에 성공하였습니다.", lifestyleResponseDto);
    }
}
