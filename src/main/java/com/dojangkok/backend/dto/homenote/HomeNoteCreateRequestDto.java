package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomeNoteCreateRequestDto {

    @NotBlank(message = "집 노트의 제목이 비어있습니다.")
    private String title;
}
