package com.dojangkok.backend.dto.homenote;

import com.dojangkok.backend.dto.checklist.ChecklistResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeNoteCreateResponseDto {

    @JsonProperty("home_note_id")
    private Long homeNoteId;

    private String title;

    private ChecklistResponseDto checklist;
}
