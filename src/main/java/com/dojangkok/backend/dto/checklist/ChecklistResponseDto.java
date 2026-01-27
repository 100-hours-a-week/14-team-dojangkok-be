package com.dojangkok.backend.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChecklistResponseDto {

    @JsonProperty("checklist_id")
    private Long checklistId;

    @JsonProperty("checklist_items")
    private List<ChecklistItemDto> checklistItems;
}
