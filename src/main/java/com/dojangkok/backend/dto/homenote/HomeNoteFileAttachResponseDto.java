package com.dojangkok.backend.dto.homenote;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeNoteFileAttachResponseDto {

    private List<HomeNoteFileAttachItemResponseDto> items;
}
