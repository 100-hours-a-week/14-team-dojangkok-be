package com.dojangkok.backend.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItemDto {

    @NotNull(message = "체크리스트 항목 ID는 필수입니다.")
    @JsonProperty("checklist_item_id")
    private Long checklistItemId;

    @NotNull(message = "체크리스트 내용은 필수입니다.")
    private String content;

    @NotNull(message = "체크리스트 상태는 필수입니다.")
    @JsonProperty("is_completed")
    private boolean completed;
}