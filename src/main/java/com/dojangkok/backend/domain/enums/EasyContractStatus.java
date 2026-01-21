package com.dojangkok.backend.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EasyContractStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}