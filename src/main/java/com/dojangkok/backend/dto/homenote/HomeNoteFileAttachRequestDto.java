package com.dojangkok.backend.dto.homenote;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class HomeNoteFileAttachRequestDto {

    @NotEmpty(message = "첨부할 파일 목록이 비어있습니다.")
    @Size(max = 50, message = "이미지는 최대 50장까지 업로드할 수 있습니다.")
    @Valid
    private List<HomeNoteFileAttachItemRequestDto> files;
}
