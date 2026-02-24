package com.dojangkok.backend.common.util;

import java.util.List;

public record PaginationResult<T>(List<T> items, boolean hasNext, String nextCursor) {
}
