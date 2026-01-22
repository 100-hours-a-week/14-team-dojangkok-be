package com.dojangkok.backend.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChecklistStatus {

    PROCESSING, COMPLETED, FAILED
}
