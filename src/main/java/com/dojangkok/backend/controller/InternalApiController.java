package com.dojangkok.backend.controller;

import com.dojangkok.backend.dto.internal.InternalPropertyResponseDto;
import com.dojangkok.backend.dto.internal.InternalUserResponseDto;
import com.dojangkok.backend.service.InternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal")
public class InternalApiController {

    private final InternalApiService internalApiService;

    @GetMapping("/users/{userId}")
    public InternalUserResponseDto getUserProfile(@PathVariable Long userId) {
        return internalApiService.getUserProfile(userId);
    }

    @GetMapping("/properties/{propertyPostId}")
    public InternalPropertyResponseDto getPropertyPostInfo(@PathVariable Long propertyPostId) {
        return internalApiService.getPropertyPostInfo(propertyPostId);
    }
}
