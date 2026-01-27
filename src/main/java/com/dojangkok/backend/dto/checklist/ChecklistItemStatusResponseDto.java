package com.dojangkok.backend.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChecklistItemStatusResponseDto {

    @JsonProperty("checklist_item_id")
    private Long checklistItemId;

    @JsonProperty("is_completed")
    private boolean completed;
}
