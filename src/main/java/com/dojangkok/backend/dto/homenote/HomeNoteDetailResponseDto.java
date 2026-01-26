package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeNoteDetailResponseDto {

    @JsonProperty("home_note")
    private HomeNoteInfoDto homeNote;

    private int limit;

    @JsonProperty("hasNext")
    private boolean hasNext;

    @JsonProperty("next_cursor")
    private String nextCursor;

    private List<HomeNoteFileItemDto> files;
}
