package com.dojangkok.backend.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PaginationResult<T> {
    private final List<T> items;
    private final boolean hasNext;
    private final String nextCursor;
}
