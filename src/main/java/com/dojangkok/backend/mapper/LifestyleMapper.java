package com.dojangkok.backend.mapper;

import com.dojangkok.backend.domain.LifestyleItem;
import com.dojangkok.backend.dto.lifestyle.LifestyleResponseDto;
import com.dojangkok.backend.dto.lifestyle.LifestyleDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LifestyleMapper {

    public LifestyleResponseDto toLifestyleResponseDto(Long memberId, List<LifestyleItem> lifestyleItems) {
        List<LifestyleDto> lifestyleDtoList = lifestyleItems.stream()
                .map(this::toLifestyleDto)
                .toList();

        return LifestyleResponseDto.builder()
                .memberId(memberId)
                .lifestyleItems(lifestyleDtoList)
                .build();
    }

    public LifestyleResponseDto toEmptyResponse(Long memberId) {
        return LifestyleResponseDto.builder()
                .memberId(memberId)
                .lifestyleItems(List.of())
                .build();
    }


    private LifestyleDto toLifestyleDto(LifestyleItem lifestyleItem) {
        return LifestyleDto.builder()
                .lifestyleItemId(lifestyleItem.getId())
                .lifestyleItem(lifestyleItem.getContent())
                .build();
    }


}
