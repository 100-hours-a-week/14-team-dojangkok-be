package com.dojangkok.backend.dto.lifestyle;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LifestyleRequestDto {

    @JsonProperty("lifestyle_items")
    @Size(max = 15, message = "등록 가능한 최대 라이프스타일 개수는 15개입니다.")
    private List<String> lifestyleItems;
}
