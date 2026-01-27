package com.dojangkok.backend.dto.checklist;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChecklistGenerateResponseDto {

    private List<String> contents;
}
