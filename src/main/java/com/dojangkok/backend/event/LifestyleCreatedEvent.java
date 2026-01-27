package com.dojangkok.backend.event;

import java.util.List;

public record LifestyleCreatedEvent(
        Long lifestyleVersionId,
        List<String> lifestyleItems
) {
}
