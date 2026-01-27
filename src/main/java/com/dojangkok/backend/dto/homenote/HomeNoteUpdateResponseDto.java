package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeNoteUpdateResponseDto {

    @JsonProperty("home_note_id")
    private Long homeNoteId;

    private String title;
}
