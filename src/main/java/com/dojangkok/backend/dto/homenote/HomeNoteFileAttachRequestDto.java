package com.dojangkok.backend.dto.homenote;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class HomeNoteFileAttachRequestDto {

    @NotEmpty(message = "첨부할 파일 목록이 비어있습니다.")
    @Valid
    private List<HomeNoteFileAttachItemRequestDto> files;
}
