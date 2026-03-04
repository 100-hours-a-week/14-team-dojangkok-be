package com.dojangkok.backend.event;

import java.util.List;

public record LifestyleCreatedEvent(
        Long memberId,
        Long lifestyleVersionId,
        List<String> lifestyleItems
) {
}
