package com.dojangkok.backend.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChecklistUpdateRequestDto {

    @Valid
    @NotEmpty(message = "체크리스트 항목은 비어있을 수 없습니다.")
    @JsonProperty("checklists")
    private List<ChecklistItemDto> checklists;

}
