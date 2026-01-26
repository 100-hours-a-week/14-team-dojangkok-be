package com.dojangkok.backend.mapper;

import com.dojangkok.backend.dto.checklist.ChecklistGenerateRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChecklistTemplateMapper {

    public ChecklistGenerateRequestDto toChecklistGenerateRequestDto(List<String> lifestyleItems) {
        return ChecklistGenerateRequestDto.builder()
                .keywords(lifestyleItems)
                .build();
    }

    public ChecklistGenerateRequestDto toEmptyChecklistGenerateRequestDto() {
        return ChecklistGenerateRequestDto.builder()
                .keywords(List.of())
                .build();
    }
}
