package com.dojangkok.backend.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChecklistItemStatusRequestDto {

    @NotNull(message = "완료 상태는 필수입니다.")
    @JsonProperty("is_completed")
    private boolean completed;
}
