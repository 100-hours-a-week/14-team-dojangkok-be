package com.dojangkok.backend.dto.checklist;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChecklistCallbackRequestDto {

    private List<String> checklists;
}
