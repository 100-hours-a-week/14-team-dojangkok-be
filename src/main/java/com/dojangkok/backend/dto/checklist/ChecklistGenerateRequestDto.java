package com.dojangkok.backend.dto.checklist;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChecklistGenerateRequestDto {

    private List<String> keywords;

}
