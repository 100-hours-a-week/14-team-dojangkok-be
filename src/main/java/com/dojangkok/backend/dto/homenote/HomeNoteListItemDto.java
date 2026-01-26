package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class HomeNoteListItemDto {

    @JsonProperty("home_note_id")
    private Long homeNoteId;

    private String title;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("file_count")
    private int fileCount;

    @JsonProperty("preview_images")
    private List<PreviewImageDto> previewImages;
}
